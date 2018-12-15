#include <linux/kernel.h>
#include <linux/module.h>
#include <linux/init.h>
#include <linux/slab.h>
#include <linux/kthread.h>

#include <linux/errno.h>
#include <linux/types.h>

#include <linux/input.h>

#include <net/bluetooth/bluetooth.h>
#include <net/bluetooth/rfcomm.h>

#include <linux/unistd.h>
#include <linux/wait.h>

#include <linux/sched/signal.h>

#include <net/sock.h>
#include <net/request_sock.h>

#define CHANNEL 1
#define MAX_CONNS 1
#define MODULE_NAME "BlueMServer"

#define DEFAULT_SHIFT 2

static int rfcomm_listener_stopped = 0;
static int rfcomm_acceptor_stopped = 0;
static int curr_conn = 0;

static struct input_dev *bluem_dev;

struct rfcomm_conn_handler_data
{
    struct sockaddr_rc *address;
    struct socket *accept_socket;
    int thread_id;
};

struct rfcomm_conn_handler
{
    struct rfcomm_conn_handler_data *data[MAX_CONNS];
    struct task_struct *thread[MAX_CONNS];
    int rfcomm_conn_handler_stopped[MAX_CONNS]; 
};

struct rfcomm_server_service
{
    int running;  
    struct socket *listen_socket;
    struct task_struct *thread;
    struct task_struct *accept_thread;
};

struct rfcomm_server_service *rfcomm_server;
struct rfcomm_conn_handler *rfcomm_conn_handler;

int handle_mouse_moving(unsigned char *buf, int len)
{
	int shift_x = 0, shift_y = 0, accel = 0;

	if (len != 3)
	{
		printk(KERN_ERR "BlueM: wrong incoming format for mouse!\n");
		return -1;
	}

	accel = buf[2] - '0';

	switch (buf[0])
	{
		case 'u':
			shift_y = -DEFAULT_SHIFT;
			break;
		case 'd':
			shift_y = DEFAULT_SHIFT;
			break;
		default:
			shift_y = 0;
	}

	switch (buf[1])
	{
		case 'l':
			shift_x = -DEFAULT_SHIFT;
			break;
		case 'r':
			shift_x = DEFAULT_SHIFT;
			break;
		default:
			shift_x = 0;
	}

	shift_x *= accel;
	shift_y *= accel;
	
	input_report_rel(bluem_dev, REL_X, shift_x);
	input_report_rel(bluem_dev, REL_Y, shift_y);
	input_sync(bluem_dev);

	return 0;
}

int rfcomm_server_receive(struct socket *sock, int id, struct sockaddr_rc *address, unsigned char *buf, int size, unsigned long flags)
{
	struct msghdr msg;
	struct kvec vec;
	int len;
	
	if (sock==NULL)
	{
		printk(KERN_ERR "BlueM: rfcomm_server_receive() -> socket is NULL!\n");
		return -1;
	}

	msg.msg_name = 0;
	msg.msg_namelen = 0;
	msg.msg_control = NULL;
	msg.msg_controllen = 0;
	msg.msg_flags = flags;

	vec.iov_len = size;
	vec.iov_base = buf;

	do 
	{
		//if (!skb_queue_empty(&sock->sk->sk_receive_queue))
			//printk(KERN_DEBUG "BlueM: is recieve queue empty? %s.\n", skb_queue_empty(&sock->sk->sk_receive_queue) ? "Yes" : "No");

		len = kernel_recvmsg(sock, &msg, &vec, size, size, flags);
	} 
	while (len == -EAGAIN || len == -ERESTARTSYS);

	printk(KERN_DEBUG "BlueM: client sent: %s\n", buf);

	return len;
}

int connection_handler(void *data)
{
	struct rfcomm_conn_handler_data *conn_data = (struct rfcomm_conn_handler_data *) data;

	struct sockaddr_rc *address = conn_data->address;
	struct socket *accept_socket = conn_data->accept_socket;
	int id = conn_data->thread_id;

	int ret, len = 64;
	unsigned char in_buf[len+1];

	DECLARE_WAITQUEUE(recv_wait, current);
	allow_signal(SIGKILL|SIGSTOP);

	while (1)
	{
		add_wait_queue(&accept_socket->sk->sk_wq->wait, &recv_wait);  
		while (skb_queue_empty(&accept_socket->sk->sk_receive_queue))
		{
			__set_current_state(TASK_INTERRUPTIBLE);
			schedule_timeout(HZ);

			if (kthread_should_stop())
			{
				__set_current_state(TASK_RUNNING);

				printk(KERN_DEBUG "BlueM: rfcomm server handle connection thread stopped.\n");

				rfcomm_conn_handler->rfcomm_conn_handler_stopped[id] = 1;

				remove_wait_queue(&accept_socket->sk->sk_wq->wait, &recv_wait);
				kfree(rfcomm_conn_handler->data[id]->address);
				kfree(rfcomm_conn_handler->data[id]);
				sock_release(rfcomm_conn_handler->data[id]->accept_socket);

				return 0;
			}

			if (signal_pending(current))
			{
				__set_current_state(TASK_RUNNING);
				remove_wait_queue(&accept_socket->sk->sk_wq->wait, &recv_wait);
				
				rfcomm_conn_handler->rfcomm_conn_handler_stopped[id] = 1;
				kfree(rfcomm_conn_handler->data[id]->address);
				kfree(rfcomm_conn_handler->data[id]);
				sock_release(rfcomm_conn_handler->data[id]->accept_socket);
				rfcomm_conn_handler->thread[id] = NULL;
				do_exit(0);
			}
			__set_current_state(TASK_RUNNING);
		}
		remove_wait_queue(&accept_socket->sk->sk_wq->wait, &recv_wait);

		memset(in_buf, 0, len+1);
		ret = rfcomm_server_receive(accept_socket, id, address, in_buf, len, MSG_DONTWAIT);
		if (ret > 0)
		{
			if (memcmp(in_buf, "BYE", 3) == 0)
			{
				printk(KERN_DEBUG "BlueM: client left.\n");
				break;
			}
			else if (memcmp(in_buf, "lk", 2) == 0)
			{
				input_report_key(bluem_dev, BTN_LEFT, 1);
				input_sync(bluem_dev);
				input_report_key(bluem_dev, BTN_LEFT, 0);
				input_sync(bluem_dev);
			}
			else if (memcmp(in_buf, "rk", 2) == 0)
			{
				input_report_key(bluem_dev, BTN_RIGHT, 1);
				input_sync(bluem_dev);
				input_report_key(bluem_dev, BTN_RIGHT, 0);
				input_sync(bluem_dev);
			}
			else 
			{
				handle_mouse_moving(in_buf, ret);
			}
		}
	}

	rfcomm_conn_handler->rfcomm_conn_handler_stopped[id] = 1;
	kfree(rfcomm_conn_handler->data[id]->address);
	kfree(rfcomm_conn_handler->data[id]);
	sock_release(rfcomm_conn_handler->data[id]->accept_socket);
	curr_conn -= 1;
	rfcomm_conn_handler->thread[id] = NULL;
	do_exit(0);
}

int rfcomm_server_accept(void)
{
	int accept_err = 0;
	struct socket *socket;
	struct socket *accept_socket = NULL;
	int id = 0;

	allow_signal(SIGKILL|SIGSTOP);

	socket = rfcomm_server->listen_socket;
	printk(KERN_DEBUG "BlueM: creating the accept socket.\n");

	while (1)
	{
		while (curr_conn != MAX_CONNS)
		{
			struct rfcomm_conn_handler_data *data = NULL;
			struct sockaddr_rc *client = NULL;
			int addr_len;

			accept_err = sock_create(socket->sk->sk_family, socket->type, socket->sk->sk_protocol, &accept_socket);

			if (accept_err < 0 || !accept_socket)
			{
				printk(KERN_ERR "BlueM: accept_error %d while creating rfcomm server accept socket!\n", accept_err);
				rfcomm_acceptor_stopped = 1;

				do_exit(0);
			}

			accept_socket->type = socket->type;
			accept_socket->ops  = socket->ops;

			while ((accept_err = socket->ops->accept(socket, accept_socket, O_NONBLOCK, false)) < 0)
			{ 
				__set_current_state(TASK_INTERRUPTIBLE);
				schedule_timeout(HZ);

				if (kthread_should_stop())
				{
					printk(KERN_DEBUG "BlueM: rfcomm server acceptor thread stopped while wait for accept.\n");
					rfcomm_acceptor_stopped = 1;
					__set_current_state(TASK_RUNNING);
					sock_release(accept_socket);

					return 0;
				}

				if (signal_pending(current))
				{
					__set_current_state(TASK_RUNNING);
					sock_release(accept_socket);
					rfcomm_acceptor_stopped = 1;

					do_exit(0);
				}
				__set_current_state(TASK_RUNNING);
			}
			curr_conn += 1;
			printk(KERN_DEBUG "BlueM: accept connection.\n");

			client = kmalloc(sizeof(struct sockaddr_rc), GFP_KERNEL);   
			memset(client, 0, sizeof(struct sockaddr_rc));
			addr_len = sizeof(struct sockaddr_rc);

			accept_err = accept_socket->ops->getname(accept_socket, (struct sockaddr *) client, &addr_len, 2);

			if (accept_err < 0)
			{
				printk(KERN_ERR "BlueM: accept_error %d in getname rfcomm server!\n", accept_err);
				sock_release(accept_socket);
				rfcomm_acceptor_stopped = 1;

				do_exit(0);
			}

			for (id = 0; id < MAX_CONNS; id++)
			{
				if (rfcomm_conn_handler->thread[id] == NULL)
					break;
			}

			printk(KERN_DEBUG "BlueM: client got free id: %d\n", id);

			if (id == MAX_CONNS)
			{
				sock_release(accept_socket);
				rfcomm_acceptor_stopped = 1;

				do_exit(0);
			}

			data = kmalloc(sizeof(struct rfcomm_conn_handler_data), GFP_KERNEL);
			memset(data, 0, sizeof(struct rfcomm_conn_handler_data));

			data->address = client;
			data->accept_socket = accept_socket;
			data->thread_id = id;

			rfcomm_conn_handler->rfcomm_conn_handler_stopped[id] = 0;
			rfcomm_conn_handler->data[id] = data;
			rfcomm_conn_handler->thread[id] = kthread_run((void *) connection_handler, (void *) data, MODULE_NAME);
		}
		
		__set_current_state(TASK_INTERRUPTIBLE);
		schedule_timeout(3*HZ);
		
		if (kthread_should_stop())
		{
			__set_current_state(TASK_RUNNING);
			pr_info(KERN_DEBUG "BlueM: rfcomm server acceptor thread stopped in accept active waiting for old client to leave.\n");
			rfcomm_acceptor_stopped = 1;

			return 0;
		}
						
		if (signal_pending(current))
		{
			__set_current_state(TASK_RUNNING);
			rfcomm_acceptor_stopped = 1;
			do_exit(0);
		}

		__set_current_state(TASK_RUNNING);
	}
}

int rfcomm_server_listen(void)
{
	int server_err;
	struct socket *conn_socket;
	struct sockaddr_rc server;

	DECLARE_WAIT_QUEUE_HEAD(wq);
	allow_signal(SIGKILL|SIGTERM);         

	server_err = sock_create(AF_BLUETOOTH, SOCK_STREAM, BTPROTO_RFCOMM, &rfcomm_server->listen_socket);
	if (server_err < 0)
	{
		printk(KERN_ERR "BlueM: error %d while creating rfcomm server listen socket!\n", server_err);
		rfcomm_listener_stopped = 1;

		do_exit(0);
	}

	conn_socket = rfcomm_server->listen_socket;
	rfcomm_server->listen_socket->sk->sk_reuse = 1;

	bacpy(&server.rc_bdaddr, BDADDR_ANY);
	server.rc_family = AF_BLUETOOTH;
	server.rc_channel = CHANNEL;

	server_err = conn_socket->ops->bind(conn_socket, (struct sockaddr*) &server, sizeof(server));

	if(server_err < 0)
	{
		printk(KERN_ERR "BlueM: error %d while binding rfcomm server listen socket!\n", server_err);
		sock_release(conn_socket);
		rfcomm_listener_stopped = 1;
		
		do_exit(0);
	}

	server_err = conn_socket->ops->listen(conn_socket, 1);

	if(server_err < 0)
	{
		printk(KERN_ERR "BlueM: error %d while listening in rfcomm server listen socket!\n", server_err);
		sock_release(conn_socket);
		rfcomm_listener_stopped = 1;
		
		do_exit(0);
	}

	rfcomm_server->accept_thread = kthread_run((void*) rfcomm_server_accept, NULL, MODULE_NAME);

	while(1)
	{
		wait_event_timeout(wq, 0, 3*HZ);

		if(kthread_should_stop())
		{
			printk(KERN_DEBUG "BlueM: rfcomm server listening thread stopped in active working.\n");
			return 0;
		}

		if(signal_pending(current))
		{
			sock_release(conn_socket);
			rfcomm_listener_stopped = 1;
			
			do_exit(0);
		}
	}
}

int rfcomm_server_start(void)
{
	rfcomm_server->running = 1;
	rfcomm_server->thread = kthread_run((void *) rfcomm_server_listen, NULL, MODULE_NAME);

	return 0;
}

static int __init bluem_init(void)
{
	int ret; 

	rfcomm_server = kmalloc(sizeof(struct rfcomm_server_service), GFP_KERNEL);
	memset(rfcomm_server, 0, sizeof(struct rfcomm_server_service));

	rfcomm_conn_handler = kmalloc(sizeof(struct rfcomm_conn_handler), GFP_KERNEL);
	memset(rfcomm_conn_handler, 0, sizeof(struct rfcomm_conn_handler));

	bluem_dev = input_allocate_device();
	if (!bluem_dev)
	{
		printk(KERN_ERR "BlueM: not enough memory for device allocation!\n");
		return -ENOMEM;
	}

	bluem_dev->evbit[0] = BIT_MASK(EV_KEY) | BIT_MASK(EV_REL);
	bluem_dev->keybit[BIT_WORD(BTN_MOUSE)] = BIT_MASK(BTN_LEFT) | BIT_MASK(BTN_RIGHT) | BIT_MASK(BTN_MIDDLE);
	bluem_dev->relbit[0] = BIT_MASK(REL_X) | BIT_MASK(REL_Y) | BIT_MASK(REL_WHEEL);
	bluem_dev->name = "BlueMMouse";

	ret = input_register_device(bluem_dev);
	if (ret)
	{
		printk(KERN_ERR "BlueM: failed to register device!\n");
		return ret;
	}

	rfcomm_server_start();

	printk(KERN_INFO "BlueM: BlueM initiated.\n");
	return 0;
}

static void __exit bluem_exit(void)
{
	int ret, id;

	if(rfcomm_server->thread == NULL)
		printk(KERN_DEBUG "BlueM: no kernel thread to kill.\n");
	else
	{
		for(id = 0; id < MAX_CONNS; id++)
		{
			if(rfcomm_conn_handler->thread[id] != NULL)
			{
				if(!rfcomm_conn_handler->rfcomm_conn_handler_stopped[id])
				{
					ret = kthread_stop(rfcomm_conn_handler->thread[id]);

					if(!ret)
						printk(KERN_DEBUG "BlueM: rfcomm server connection handler thread %d stopped at exit.\n", id);
				}
			}
		}

		if(!rfcomm_acceptor_stopped)
		{
			ret = kthread_stop(rfcomm_server->accept_thread);
			if(!ret)
				printk(KERN_DEBUG "BlueM: rfcomm server acceptor thread stopped at exit.\n");
		}

		if(!rfcomm_listener_stopped)
		{
			ret = kthread_stop(rfcomm_server->thread);
			if(!ret)
				printk(KERN_DEBUG "BlueM: rfcomm server listening thread stopped at exit.\n");
		}

		if(rfcomm_server->listen_socket != NULL && !rfcomm_listener_stopped)
		{
			sock_release(rfcomm_server->listen_socket);
			rfcomm_server->listen_socket = NULL;
		}

		kfree(rfcomm_conn_handler);
		kfree(rfcomm_server);
		rfcomm_server = NULL;
	}

	input_unregister_device(bluem_dev);

	printk(KERN_INFO "BlueM: BlueM module unloaded.\n");
}

module_init(bluem_init);
module_exit(bluem_exit);

MODULE_LICENSE("GPL");
MODULE_AUTHOR("Nazarenko Nikita");
