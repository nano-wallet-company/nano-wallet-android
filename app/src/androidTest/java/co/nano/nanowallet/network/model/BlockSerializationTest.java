package co.nano.nanowallet.network.model;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.junit.Test;

import co.nano.nanowallet.network.model.request.block.OpenBlock;

import static org.junit.Assert.assertEquals;

public class BlockSerializationTest {
    @Test
    public void preserveFieldOrder() {
        String privateKey = "C5469190B25E850CED298E57723258F716A4E1956AC2BC60DA023300476D1212";
        String source = "3CD78EE059E404252669B37E8195C7AD4FC6CAEA5AA2C0A4989CF9AB248B4949";
        String representative = "xrb_3crzecs58y9gd1ucqcfcdsh56ywty5ixzqk41oa5d3i1ggm4bd6c9q5u34m3";
        //String work = "0000000000000000";

        OpenBlock openBlock = new OpenBlock(privateKey, source, representative);
        //openBlock.setWork(work);

        JsonObject expected = new JsonObject();
        expected.addProperty("type", "open");
        expected.addProperty("source", "3CD78EE059E404252669B37E8195C7AD4FC6CAEA5AA2C0A4989CF9AB248B4949");
        expected.addProperty("representative", "xrb_3crzecs58y9gd1ucqcfcdsh56ywty5ixzqk41oa5d3i1ggm4bd6c9q5u34m3");
        expected.addProperty("account", "xrb_39c99zaeon8n9qdsq9ywojqytnhfwzmr6yfaa734k369sy56q4whb1iu45sg");
        //expected.addProperty("work", "0000000000000000");
        expected.addProperty("signature", "1C0F0BADDB432D5DB3862D58801D87CCB8915165D2BE02434EB76F2EC71C7B6E66F39964942FF1F0B15D2C6B53A8C73A03785ACEB78574C762EF3354E09D0408");

        Gson gson = new Gson();
        assertEquals(expected, gson.toJsonTree(openBlock));
    }
}
