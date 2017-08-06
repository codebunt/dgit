package org.kapsarc.dgit;

import org.kapsarc.dgit.ebean.RootModel;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.config.DataSourceConfig;
import com.avaje.ebean.config.ServerConfig;

public class DGitConnection {

	private static final ThreadLocal<DGitConnection> threadLocal = new ThreadLocal<DGitConnection>();
	private String userid;

	static {
		//createEbeanServer();

	}

	private static EbeanServer createEbeanServer() {
		ServerConfig c = new ServerConfig();
		c.setName("pg");

		DataSourceConfig pgdb = new DataSourceConfig();
		pgdb.setDriver("org.postgresql.Driver");
		pgdb.setUsername("dgitadmin");
		pgdb.setPassword("dgitadmin");
		pgdb.setUrl("jdbc:postgresql://127.0.0.1:5432/dgit");
		pgdb.setHeartbeatSql("select count(*) from dual");

		c.setDdlGenerate(false); 
		c.setDdlRun(false); 
		c.setDefaultServer(false);
		c.setRegister(true);

		c.setDataSourceConfig(pgdb);
		c.addClass(RootModel.class);
		c.addPackage("org.kapsarc.digit.ebean");
		return EbeanServerFactory.create(c);
	}

	public DGitConnection(String userid) {
		this.userid = userid;
		threadLocal.set(this);
	}

	public static void destroy() {
		threadLocal.remove();
	}

	public static DGitConnection get() {
		return threadLocal.get();
	}

	public EbeanServer getEbeanServer() {
		return Ebean.getDefaultServer();
	}

}
