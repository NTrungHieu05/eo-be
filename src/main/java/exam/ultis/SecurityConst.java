package exam.ultis;

public class SecurityConst {
	public static final String AUTHORIZATION_HEADER = "Authorization";
	public static final String BASIC_TOKEN = "Basic ";
	public static final String BEARER_TOKEN = "Bearer ";

	public static final String AUTHENTICATION = "authentication";
	public static final String ACCESS_TOKEN = "accessToken";

	public static final int ACCESS_TOKEN_VALIDITY_SECONDS = 60 * 60 * 24 * 30;
}
