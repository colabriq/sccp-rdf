//package com.goodforgoodbusiness.endpoint;
//
//import com.goodforgoodbusiness.endpoint.graph.dht.container.StorableGraphContainer;
//
//public class StorableGraphContainerTest {
//	public static void main(String[] args) {
////		var trup = new Triple(
////			NodeFactory.createURI("https://twitter.com/ijmad8x"),
////			NodeFactory.createLiteral("foo"),
////			Node.ANY
////		);
////		
////		System.out.println(
////			DHTRepr.encodeMatchesPattern(trup)
////		);
//		
//		var response = "[{" + 
//			" \"inner_envelope\": {" + 
//			"  \"contents\": {" + 
//			"   \"type\": \"CLAIM\"," + 
//			"   \"added\": [" + 
//			"    {" + 
//			"     \"s\": {" + 
//			"      \"uri\": \"https://twitter.com/ijmad8x\"" + 
//			"     }," + 
//			"     \"p\": {" + 
//			"      \"uri\": \"http://xmlns.com/foaf/0.1/age\"" + 
//			"     }," + 
//			"     \"o\": {" + 
//			"      \"literal\": {" + 
//			"       \"value\": \"35\"," + 
//			"       \"type\": {" + 
//			"        \"uri\": \"http://www.w3.org/2001/XMLSchema#integer\"" + 
//			"       }" + 
//			"      }" + 
//			"     }" + 
//			"    }," + 
//			"    {" + 
//			"     \"s\": {" + 
//			"      \"uri\": \"https://twitter.com/ijmad8x\"" + 
//			"     }," + 
//			"     \"p\": {" + 
//			"      \"uri\": \"http://xmlns.com/foaf/0.1/name\"" + 
//			"     }," + 
//			"     \"o\": {" + 
//			"      \"literal\": {" + 
//			"       \"value\": \"Ian Maddison\"" + 
//			"      }" + 
//			"     }" + 
//			"    }" + 
//			"   ]," + 
//			"   \"removed\": []," + 
//			"   \"antecedents\": [\"foobar\", \"aruba\"]," + 
//			"   \"link_secret\": {" + 
//			"    \"key\": \"050268b6f30c833b449ee675f49411c7ce9c55e48959e050637ccda534a040f7\"," + 
//			"    \"alg\": \"prime256v1\"" + 
//			"   }" + 
//			"  }," + 
//			"  \"hashkey\": \"12fb6d8b5ba45392cb1f8eb0660a24f124d75f479e1a12921f80ad4ff2dbc04028df6ace1773cec5358c3a9650d91fd32c89455139235b9cf2255259cb55c59d\"," + 
//			"  \"link_verifier\": {" + 
//			"   \"key\": \"24f2620f863ab76f358a1958dcef87337f056c6beef8da53003e21042e4a1008ea3ee2244901332e0866413c42b7a9cdcb9aeb7f60aeb5c2c4f54dd33df7879f\"," + 
//			"   \"alg\": \"prime256v1\"" + 
//			"  }," + 
//			"  \"signature\": {" + 
//			"   \"did\": \"did:sov:ab1321c\"," + 
//			"   \"alg\": \"prime256v1\"," + 
//			"   \"sig\": \"46d089f1ae7d8a5798f422f7b1988f8844def285ed16ec993d9c430c323cd5827bf51f031662a90ca2cfea4c989db71b4fce404bbfdf7f7fed2fa4110015c2a4\"" + 
//			"  }" + 
//			" }," + 
//			" \"links\": []," + 
//			" \"signature\": {" + 
//			"  \"did\": \"did:sov:ab1321c\"," + 
//			"  \"alg\": \"prime256v1\"," + 
//			"  \"sig\": \"1a107ea1d55bf2026635f109d15ff881ce73b7cdedff6b7a11ffda1963268e4801e06db658d94d20417c24fd4c48867638d36b629fc6d4506561b9b612f469dc\"" + 
//			" }" + 
//			"}]"
//			;
//		
//		var containers = StorableGraphContainer.toStorableGraphContainers(response);
//		for (StorableGraphContainer container : containers) {
//			System.out.println(container);
//			System.out.println(container.toGraph().size());
//		}
//	}
//}
