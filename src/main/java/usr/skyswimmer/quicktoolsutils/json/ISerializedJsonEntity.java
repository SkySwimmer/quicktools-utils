package usr.skyswimmer.quicktoolsutils.json;

import java.io.IOException;

import com.google.gson.JsonObject;

public interface ISerializedJsonEntity {

	public void loadFromJson(JsonObject source, String scope) throws IOException;

}
