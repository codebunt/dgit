package controllers;

import org.kapsarc.dgit.Column;
import org.kapsarc.dgit.DGitConnection;
import org.kapsarc.dgit.Schema;
import org.kapsarc.dgit.Table;
import org.kapsarc.dgit.Workspace;

import play.mvc.Controller;
import play.mvc.Result;

public class HomeController extends Controller {

	public Result index() throws Exception {
		new DGitConnection("123");
		Schema schema = Schema.get("test", null, "main");
		schema.save();
		Workspace workSpace = schema.getWorkSpace();
		Table table = workSpace.addTable("first_table");
		table.removeColumn("first_column");
		workSpace.save();
		return ok();
	}

}
