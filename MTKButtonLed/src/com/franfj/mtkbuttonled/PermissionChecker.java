package com.franfj.mtkbuttonled;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.Runtime;

public class PermissionChecker {
	public static final int BUFF_LEN = 512;
	public static boolean isWritable(String _file){
	try {
		Process p = Runtime.getRuntime().exec("/system/bin/sh");
		DataOutputStream stdin = new DataOutputStream(p.getOutputStream());
		stdin.writeBytes("ls -l " + _file + "\n"); // \n executes the command
		stdin.flush();
		InputStream stdout = p.getInputStream();
		byte[] buffer = new byte[BUFF_LEN];
		int read;
		String out = new String();
		//read method will wait forever if there is nothing in the stream
		//so we need to read it in another way than while((read=stdout.read(buffer))>0)
		while(true){
		    read = stdout.read(buffer);
		    out += new String(buffer, 0, read);
		    if(read<BUFF_LEN){
		        //we have read everything
		        break;
		    }
		}
		//do something with the output
		//Log.d("MTKLED",out);
		if (out.charAt(8) == 'w'){
			return true;
		}
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	return false;
	}
	
	
}
