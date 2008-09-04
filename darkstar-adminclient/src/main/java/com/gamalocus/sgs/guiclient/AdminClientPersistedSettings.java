package com.gamalocus.sgs.guiclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class AdminClientPersistedSettings extends Properties {

	private static Properties defaultProps = new Properties();
	private static AdminClientPersistedSettings instance;
	
	static
	{
		defaultProps.put("host", "localhost");
		defaultProps.put("port", "6677");
		defaultProps.put("user", "adminuser");
		defaultProps.put("pass", "adminpass");
	}

	private File settingsFile;

	private AdminClientPersistedSettings()
	{
		super(defaultProps);
		
		settingsFile = new File("adminclient.properties");
		if(settingsFile.exists() && settingsFile.isFile())
		{
			try {
				FileInputStream in;
				load(in = new FileInputStream(settingsFile));
				in.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static AdminClientPersistedSettings getInstance()
	{
		if(instance == null)
		{
			instance = new AdminClientPersistedSettings();	
		}
		return instance;
	}
	
	public void save()
	{
		try {
			FileOutputStream out;
			store(out = new FileOutputStream(settingsFile, false), "Saved GUI Admin Client Settings");
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
