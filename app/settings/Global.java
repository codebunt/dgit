package settings;

import java.io.File;

import org.kapsarc.dgit.dto.FailureResponse;
import org.kapsarc.dgit.exception.BadRequest;
import org.kapsarc.dgit.exception.NoContent;
import org.kapsarc.dgit.exception.NotFound;

import play.Application;
import play.GlobalSettings;
import play.libs.F.Promise;
import play.libs.Json;
import play.mvc.Http.RequestHeader;
import play.mvc.Result;
import play.mvc.Results;

public class Global extends GlobalSettings {

	@Override
	public void onStart(Application app) {
		loadPaymentConfig(app);
		super.onStart(app);
	}

	private void loadPaymentConfig(Application app) {
		try {
			File f = new File("./conf/dgit.props");
			DgitConfig.load(f);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@Override
	public Promise<Result> onError(RequestHeader app, Throwable e) {
		e.printStackTrace();
		// TODO : Send email to ourselves
		do {
			if (e instanceof BadRequest) {
				return Promise.<Result> pure(Results.status(400, Json.toJson(new FailureResponse(e.getMessage()))));
			}
			if (e instanceof NotFound) {
				return Promise.<Result> pure(Results.status(404, Json.toJson(new FailureResponse(e.getMessage()))));
			}
			if (e instanceof NoContent) {
				return Promise.<Result> pure(Results.status(204, Json.toJson(new FailureResponse(e.getMessage()))));
			}
			e = e.getCause();
		} while (e!=null && e.getCause() != null);
		return Promise.<Result> pure(Results.status(500, Json.toJson(new FailureResponse("Unexpected Error"))));
	}

	@Override
	public Promise<Result> onHandlerNotFound(RequestHeader app) {
		return Promise.<Result> pure(Results.status(404, Json.toJson(new FailureResponse("Not found"))));
	}

}
