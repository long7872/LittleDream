package Entity;

import java.net.URL;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import Controller.Control;

public class HibernateUtil {
	private static final SessionFactory SESSION_FACTORY = buildSessionFactory();

	public static SessionFactory buildSessionFactory() 
	{
		try 
		{
			URL url = Control.class.getResource("/hibernate.cfg.xml");
			return new Configuration().configure(url).buildSessionFactory();
		} catch (Exception e) 
		{
			System.out.println("Null");
			e.printStackTrace();
			return null;
		}
	}
	
	public static SessionFactory getSessionFactory()
	{
		return SESSION_FACTORY;
	}
	
	public static void shutdown()
	{
		getSessionFactory().close();
	}
}