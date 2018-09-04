#include <netinet/in.h>
#include <arpa/inet.h>
#include <sys/types.h>
#include <sys/socket.h>

#include <bluetooth/bluetooth.h>
#include <bluetooth/rfcomm.h>

#define CHANNEL 1
#define QUEUE 1
#define SIZE 256

int main(void)
{
  	int sock, client, alen, bytes;
  	struct sockaddr_rc addr;
	char buf[SIZE];	

  	if( (sock = socket(AF_BLUETOOTH, SOCK_STREAM, BTPROTO_RFCOMM)) < 0)
   	{
      	perror("socket");
      	exit(1);
   	}

  	addr.rc_family = AF_BLUETOOTH;
  	bacpy(&addr.rc_bdaddr, BDADDR_ANY);
  	addr.rc_channel = htobs(CHANNEL);
  	alen = sizeof(addr);

  	if(bind(sock, (struct sockaddr *)&addr, alen) < 0)
  	{
      	perror("bind");
      	exit(1);
   	}

  	listen(sock, QUEUE);
  	printf("Waiting for connections...\n\n");  

  	while(client = accept(sock, (struct sockaddr *)&addr, &alen))
   	{
      	printf("Got a connection attempt!\n");
		int flag = 1;
		while (flag)
		{
			bytes = read(client, buf, sizeof(buf));
			if (bytes < 0)
			{
				flag = 0;
			}
			else
			{			
				buf[bytes] = 0;
				printf("Client sent: %s\n", buf);
			}
		}
		printf("Client left!\n");
      	close(client);
   	}

  	close(sock);
  	return 0;
}
