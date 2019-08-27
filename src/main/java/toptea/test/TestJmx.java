package toptea.test;

import java.lang.management.MemoryUsage;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

public class TestJmx {

	public static void main(String[] args) {

		//线程池数量可自行更改测试
		ExecutorService threadPool = Executors.newFixedThreadPool(10);

		for (int i = 1; i <= 10; i++) {

			threadPool.execute(new Runnable() {

				public void run() {
					
                    //每个线程循环调用次数可自行更改测试
					for (int i = 1; i <= 1; i++) {
						jmxTest(i);

					}

				}
			});
		}

	}

	public static void jmxTest(int colNum) {

		try {
            
			// 最大堆内存
			long heapMaxMemory = 0;
			// 已分配堆内存
			long heapCommitMemory = 0;
			// 已使用堆内存
			long heapUsedMemory = 0;
			// 堆内存占用率(百分比)
			String heapPercentage = "N/A";
			
			
			// 最大非堆内存
			long nonHeapMaxMemory = 0;
			// 已分配非堆内存
			long nonCommitMemory = 0;
			// 已使用非堆内存
			long nonHeapUsed = 0;
			// 非堆内存占用率(百分比)
			String nonHeapPercentage = "N/A";
			
			
			// 最大线程数
			String maxThreads = "N/A";
			// 当前线程池数量
			String currentThreadCount = "N/A";
			// 当前活动线程数
			String currentThreadsBusy = "N/A";
			
            
			// 虚拟机厂商
			String VmVendor = "N/A";
			// 虚拟机名称
			String VmName = "N/A";
			// 虚拟机版本
			String VmVersion = "N/A";
			// 开始运行时间
			String startTime = "0";
			// 连续运行时间
			String timeSpan = "N/A";
			

            // jmx远程连接地址，针对不同的测试主机可自行修改其中的IP和端口
			String jmxURL = "service:jmx:rmi:///jndi/rmi://148.70.239.83:8992/jmxrmi";
            
			JMXServiceURL serviceURL = new JMXServiceURL(jmxURL);

			Map map = new HashMap();
			String[] credentials = new String[] { "monitorRole", "tomcat" };
			map.put("jmx.remote.credentials", credentials);
            
			// 连接远程主机
			JMXConnector connector = JMXConnectorFactory.connect(serviceURL, map);
			MBeanServerConnection mbsc = connector.getMBeanServerConnection();

			// 堆内存
			ObjectName Memory = new ObjectName("java.lang:type=Memory");
			MemoryUsage heapMemoryUsage = MemoryUsage
					.from((CompositeDataSupport) mbsc.getAttribute(Memory, "HeapMemoryUsage"));
			// 获取最大堆内存信息
			heapMaxMemory = heapMemoryUsage.getMax();
			// 获取已分配堆内存信息
			heapCommitMemory = heapMemoryUsage.getCommitted();
			// 获取已使用堆内存信息
			heapUsedMemory = heapMemoryUsage.getUsed();
			// 计算堆内存占用率(百分比)信息
			heapPercentage = (double) heapUsedMemory * 100 / heapCommitMemory + "%";

			// 非堆内存
			MemoryUsage nonheapMemoryUsage = MemoryUsage
					.from((CompositeDataSupport) mbsc.getAttribute(Memory, "NonHeapMemoryUsage"));
			// 获取最大非堆内存信息
			nonHeapMaxMemory = nonheapMemoryUsage.getMax();
			// 获取已分配非堆内存信息
			nonCommitMemory = nonheapMemoryUsage.getCommitted();
			// 获取已使用非堆内存信息
			nonHeapUsed = nonheapMemoryUsage.getUsed();
			// 计算非堆内存占用率(百分比)信息
			nonHeapPercentage = (double) nonHeapUsed * 100 / nonCommitMemory + "%";

			// 线程池
			ObjectName threadpoolObjName = new ObjectName("Catalina:type=ThreadPool,name=\"*http*\"");

			Set<ObjectName> s2 = mbsc.queryNames(threadpoolObjName, null);

			for (ObjectName obj : s2) {

				ObjectName objname = new ObjectName(obj.getCanonicalName());
                // 获取最大线程数
				maxThreads = String.valueOf(mbsc.getAttribute(objname, "maxThreads"));
				// 获取当前线程池数量
				currentThreadCount = String.valueOf(mbsc.getAttribute(objname, "currentThreadCount"));
				// 获取当前活动线程数
				currentThreadsBusy = String.valueOf(mbsc.getAttribute(objname, "currentThreadsBusy"));
			}

			// 运行状态
			ObjectName runtimeObjName = new ObjectName("java.lang:type=Runtime");
			VmVendor = (String) mbsc.getAttribute(runtimeObjName, "VmVendor");
			VmName = (String) mbsc.getAttribute(runtimeObjName, "VmName");
			VmVersion = (String) mbsc.getAttribute(runtimeObjName, "VmVersion");

			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			startTime = df.format(new Date((Long) mbsc.getAttribute(runtimeObjName, "StartTime")));
			timeSpan = formatTimeSpan((Long) mbsc.getAttribute(runtimeObjName, "Uptime"));

			// 输出到控制台
			System.out.println
			("---------------------" + " 系统当前时间:  " + getCurTime() + " ------------------------\n"
				   + "线程名" + Thread.currentThread().getName() + "  第" + colNum + "次采集结果:" + "\n" 
			       + "堆内存使用情况:" + "  最大堆内存(字节):" + heapMaxMemory + "  已分配堆内存(字节):" + heapCommitMemory + "  已使用堆内存(字节):" + heapUsedMemory + "  堆内存占用率(百分比):" + heapPercentage + "\n"
				   + "非堆内存使用情况:" + "  最大非堆内存(字节):" + nonHeapMaxMemory + "  已分配非堆内存(字节):" + nonCommitMemory + "  已使用非堆内存(字节):" + nonHeapUsed + "  非堆内存占用率(百分比):" + nonHeapPercentage + "\n"
			       + "线程池使用情况:" + "  最大线程数:" + maxThreads + "  当前线程池数量:" + currentThreadCount + "  当前活动线程数：" + currentThreadsBusy + "\n"
				   + "运行状态:" + "  开始运行时间:" + startTime + "  连续运行时间:" + timeSpan + "  虚拟机厂商:" + VmVendor + "  虚拟机名称:" + VmName + "  虚拟机版本:" + VmVersion);
			
		} catch (Exception e) {

			e.printStackTrace();
		}

	}
	
	public static String getCurTime()
	{
		SimpleDateFormat sdf = new SimpleDateFormat();
	    sdf.applyPattern("yyyy-MM-dd HH:mm:ss a");
	    Date date = new Date();// 获取当前时间 
	    return sdf.format(date);
	}

	public static String formatTimeSpan(Long span) {
		long minseconds = span % 1000;

		span = span / 1000;
		long seconds = span % 60;

		span = span / 60;
		long mins = span % 60;

		span = span / 60;
		long hours = span % 24;

		span = span / 24;
		long days = span;
		return (new Formatter()).format("%1$d天 %2$02d小时%3$02d分%4$02d.%5$03d秒", days, hours, mins, seconds, minseconds)
				.toString();
	}

}
