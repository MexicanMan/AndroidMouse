#include <linux/kernel.h>
#include <linux/module.h>
#include <linux/init.h>
#include <linux/slab.h>
#include <asm/atomic.h>
#include <linux/input.h>
#include <linux/uaccess.h>

static struct input_dev *mouset_dev;
int major;

int mouset_open(struct inode *inode, struct file *filp)
{
    printk(KERN_INFO "MOUSET: faking an USB mouse via the char device\n");
	try_module_get(THIS_MODULE);
	
	return 0;
}    

int mouset_release(struct inode *inode, struct file *filp)
{
    printk(KERN_INFO "MOUSET: closing char device\n");
    module_put(THIS_MODULE);
	
	return 0;
}

ssize_t mouset_read(struct file *filp, char *buffer, size_t length, loff_t * offset)
{
    printk(KERN_INFO "MOUSET: No implementation\n");
	return 0;
}

ssize_t mouset_write(struct file *filp, const char *buf, size_t count, loff_t *offp)
{
	static char localbuf[2];
	copy_from_user(localbuf, buf, count);

	switch (localbuf[0]) {
		case 'u': 
		{
			input_report_rel(mouset_dev, REL_Y, -10);
			break;
		}
		case 'd':
		{
			input_report_rel(mouset_dev, REL_Y, 10);
			break;
		}
	}

	input_sync(mouset_dev);
	
	return count;
}

struct file_operations mouset_fops = {
	write:   mouset_write,
	read:    mouset_read,
	open:    mouset_open,
	release: mouset_release,
};

static int __init mouset_init(void)
{
	int ret;

	major = register_chrdev(0, "Mouset", &mouset_fops);
	if (major < 0) 
	{
	  printk(KERN_ERR "MOUSET: Registering char device failed with %d\n", major);
	  return major;
	}

	mouset_dev = input_allocate_device();
	if (!mouset_dev)
	{
		printk(KERN_ERR "MOUSET: Not enough memory\n");
		return -ENOMEM;
	}

	mouset_dev->evbit[0] = BIT_MASK(EV_REL);
	mouset_dev->relbit[0] = BIT_MASK(REL_X) | BIT_MASK(REL_Y) | BIT_MASK(REL_WHEEL);
	mouset_dev->name = "Mouset";

	ret = input_register_device(mouset_dev);
	if (ret)
	{
		printk(KERN_ERR "MOUSET: Failed to register device\n");
		return ret;
	}

	printk(KERN_DEBUG "MOUSET: mouset has been loaded\n");
	printk(KERN_INFO "Major number is %d. To use the\n", major);
	printk(KERN_INFO "driver, create a file with\n");   
    printk(KERN_INFO "'mknod /dev/%s c %d 0'.\n", "Mouset", major);

    return 0;
}

static void __exit mouset_exit(void)
{
	input_unregister_device(mouset_dev);
	unregister_chrdev(major, "Mouset");

    printk(KERN_DEBUG "MOUSET: mouset has been unloaded\n");
}

module_init(mouset_init);
module_exit(mouset_exit);

MODULE_LICENSE("GPL");
MODULE_AUTHOR("Nazarenko Nikita");
MODULE_DESCRIPTION("Temp mouse driver");
