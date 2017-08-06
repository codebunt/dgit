package controllers;

import org.kapsarc.dgit.DGitConnection;
import org.kapsarc.dgit.Schema;

import play.mvc.Controller;
import play.mvc.Result;

public class HomeController extends Controller {

	public Result index() {
		new DGitConnection("123");
		Schema s = Schema.get("test", "123");
		return ok();
	}

}
