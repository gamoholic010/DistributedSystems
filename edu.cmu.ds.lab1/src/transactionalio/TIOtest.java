package transactionalio;

import java.lang.reflect.Constructor;

import processmanager.MigratableProcess;

import com.sun.xml.internal.fastinfoset.util.StringArray;

import grepprocess.GrepProcess;

public class TIOtest {
	public static void main(String atgs[]) throws Exception{
		String[] arrv = {"of","C:/test.txt","C:/javastuff/output.txt"};
//		GrepProcess gp = new GrepProcess(arrv);
//		new Thread(gp).start();	
		
		Class<?> userClass = Class.forName("grepprocess.GrepProcess");
		Constructor<?> constructorNew = userClass.getConstructor(String[].class);
		MigratableProcess instance = (MigratableProcess)constructorNew.newInstance((Object)arrv);
		new Thread(instance).start();
		
	}
}
