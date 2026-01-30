package usr.skyswimmer.githubwebhooks.api.config;

import java.io.IOException;

import com.google.gson.JsonObject;

public interface ISerializedJsonEntity {

	public void loadFromJson(JsonObject source, String scope) throws IOException;

}
