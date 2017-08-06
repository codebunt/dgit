package settings;

import javax.inject.Inject;
import play.filters.cors.CORSFilter;

import play.api.mvc.EssentialFilter;
import play.filters.csrf.CSRFFilter;
import play.http.HttpFilters;

public class CorsFilter implements HttpFilters {

    @Inject
    CORSFilter corsFilter;

    public EssentialFilter[] filters() {
        return new EssentialFilter[] { corsFilter };
    }

}
