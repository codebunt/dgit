package controllers;

import org.kapsarc.dgit.DGitConnection;
import org.kapsarc.dgit.Schema;
import org.kapsarc.dgit.ebean.SchemaModel;

import com.avaje.ebean.EbeanServer;

import play.mvc.Controller;
import play.mvc.Result;

public class HomeController extends Controller {

	public Result index() throws Exception {
		new DGitConnection("123");
		EbeanServer ebeanServer = DGitConnection.get().getEbeanServer();
		Schema s = Schema.get("test", "123"); 
		ebeanServer.beginTransaction();

		SchemaModel sm = new SchemaModel();
		sm.branch = "a";
		sm.name = "name";
		sm.save();
		ebeanServer.commitTransaction();
		return ok();
	}

}
