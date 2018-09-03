import com.github.ontio.OntSdk;
import com.github.ontio.account.Account;
import com.github.ontio.common.Helper;
import com.github.ontio.core.transaction.Transaction;
import com.github.ontio.network.exception.ConnectorException;
import net.sf.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;

public class TestDemo {

	static String privatekey0 = "523c5fcf74823831756f0bcb3634234f10b3beb1c05595058534577752ad2d9f";
	static String privatekey1 = "49855b16636e70f100cc5f4f42bc20a6535d7414fb8845e7310f8dd065a97221";
	static String privatekey2 = "1094e90dd7c4fdfd849c14798d725ac351ae0d924b29a279a9ffa77d5737bd96";
	static String privatekey3 = "bc254cf8d3910bc615ba6bf09d4553846533ce4403bc24f58660ae150a6d64cf";
	static String privatekey4 = "06bda156eda61222693cc6f8488557550735c329bc7ca91bd2994c894cd3cbc8";
	static String privatekey5 = "f07d5a2be17bde8632ec08083af8c760b41b5e8e0b5de3703683c3bdcfb91549";
	static String privatekey6 = "6c2c7eade4c5cb7c9d4d6d85bfda3da62aa358dd5b55de408d6a6947c18b9279";
	static String privatekey7 = "24ab4d1d345be1f385c75caf2e1d22bdb58ef4b650c0308d9d69d21242ba8618";
	static String privatekey8 = "87a209d232d6b4f3edfcf5c34434aa56871c2cb204c263f6b891b95bc5837cac";
	static String privatekey9 = "1383ed1fe570b6673351f1a30a66b21204918ef8f673e864769fa2a653401114";

	public OntSdk getOntSdk() throws Exception {
		String ip = "http://polaris1.ont.io";
		String restUrl = ip + ":" + "20334";
		String rpcUrl = ip + ":" + "20336";
//		String wsUrl = ip + ":" + "20335";

		OntSdk wm = OntSdk.getInstance();
		wm.setRpc(rpcUrl);
		wm.setRestful(restUrl);
		wm.setDefaultConnect(wm.getRestful());

		return wm;
	}

	@Test
	public void createAcct() throws Exception {
		OntSdk sdk = this.getOntSdk();

		Account newAcct = new Account(sdk.defaultSignScheme);

		System.out.println(newAcct.getAddressU160().toBase58());
		System.out.println(newAcct.getAddressU160().toString());
		System.out.println(newAcct.getAddressU160().toHexString());

		System.out.println(Helper.toHexString(newAcct.serializePrivateKey()));
		System.out.println(Helper.toHexString(newAcct.serializePublicKey()));

		long balance = sdk.nativevm().ont().queryBalanceOf(newAcct.getAddressU160().toBase58());

		System.out.println("balance:" + balance);
	}

	@Test
	public void testAccount() throws Exception {
		OntSdk ontSdk = getOntSdk();

		Account acct0 = new Account(Helper.hexToBytes(privatekey0), ontSdk.defaultSignScheme);
		Account acct1 = new Account(Helper.hexToBytes("fda78ad366f83d67bb3087cd2ac83f920084ae52d37872edc6cd8f5f05d88673"), ontSdk.defaultSignScheme);

//		System.out.println(acct0.getPublicKey().getFormat() + " private key: "+ acct0.getPrivateKey().getFormat());

		long bal = ontSdk.nativevm().ont().queryBalanceOf(acct0.getAddressU160().toBase58());

		System.out.println("acct0:" + bal);

		long amount = 100;

//		String rs = ontSdk.nativevm().ont().sendTransfer(acct0, acct1.getAddressU160().toBase58(), amount, acct0, ontSdk.DEFAULT_GAS_LIMIT, 500);

//		System.out.println(rs);

//		ontSdk.getConnect().syncSendRawTransaction(rs);
		// 转账(1.构造转账事务 2.账号签名)
		Transaction tx = ontSdk.nativevm().ont().makeTransfer(acct0.getAddressU160().toBase58(),"ANRmKjbsP3hruBRC79UNKDcnwApi6WFei3",
				amount, acct0.getAddressU160().toBase58(), ontSdk.DEFAULT_GAS_LIMIT, 500);
		tx = ontSdk.signTx(tx, new Account[][]{{acct0}});

		// 同步转账交易
//		ontSdk.getConnect().sendRawTransaction(tx);
		Object o = ontSdk.getConnect().sendRawTransactionSync(tx.toHexString());

		JSONObject result = JSONObject.fromObject(o);
		System.out.println(result.toString());
//
//
//		Object obj = ontSdk.getConnect().getMemPoolTxState(rs);
//		System.out.println(JSONObject.fromObject(obj).toString());
//
//		Object smartCodeEvent = ontSdk.getConnect().getSmartCodeEvent(rs);
//		System.out.println(JSONObject.fromObject(smartCodeEvent).toString());

		long b = ontSdk.nativevm().ont().queryBalanceOf(acct0.getAddressU160().toBase58());
		System.out.println("balance:" + b);

		long b1 = ontSdk.nativevm().ont().queryBalanceOf(acct1.getAddressU160().toBase58());
		System.out.println("balance:" + b1);
/*
		System.out.println(tx.json());
		System.out.println(tx.hash().toString());

		ontSdk.signTx(tx, new com.github.ontio.account.Account[][]{{acct0}});

		System.out.println(tx.toHexString());

		//发送预执行（可选）
		Object obj = ontSdk.getConnect().sendRawTransactionPreExec(tx.toHexString());
		System.out.println(JSONObject.fromObject(obj).toString());

		//发送交易
		obj = ontSdk.getConnect().sendRawTransaction(tx.toHexString());
		System.out.println(JSONObject.fromObject(obj).toString());

		//同步发送交易
		obj = ontSdk.getConnect().syncSendRawTransaction(tx.toHexString());
		System.out.println(JSONObject.fromObject(obj).toString());
		*/
	}

	@Test
	public void testOnt() {
		System.out.println("test ont..");

		String ip = "http://polaris1.ont.io";
		// rpcurl
		String rpcUrl = ip + ":" + "20336";
		OntSdk ontSdk = OntSdk.getInstance();
		ontSdk.setRpc(rpcUrl);
		try {
			ontSdk.setDefaultConnect(ontSdk.getRpc());

			Object o = ontSdk.getConnect().getBalance("AVcv8YBABi9m6vH7faq3t8jWNamDXYytU2");

			System.out.println(o.toString());

			long ontBalance = ontSdk.nativevm().ont().queryBalanceOf("AVcv8YBABi9m6vH7faq3t8jWNamDXYytU2");

			System.out.println("ont :" + ontBalance);

			System.out.println(ontSdk.nativevm().ont().queryName());
			System.out.println(ontSdk.nativevm().ont().querySymbol());
			System.out.println(ontSdk.nativevm().ont().queryDecimals());
			System.out.println(ontSdk.nativevm().ont().queryTotalSupply());

			//ong
			System.out.println(ontSdk.nativevm().ong().queryName());
			System.out.println(ontSdk.nativevm().ong().querySymbol());
			System.out.println(ontSdk.nativevm().ong().queryDecimals());
			System.out.println(ontSdk.nativevm().ong().queryTotalSupply());
//			Address sender = Address.parse("AVcv8YBABi9m6vH7faq3t8jWNamDXYytU2");
////			Address recver = Address.parse("AXK2KtCfcJnSMyRzSwTuwTKgNrtx5aXfFX");
////			Transaction tx = ontSdk.nativevm().ont().makeTransfer(sender.toBase58(), recver.toBase58(), 10000, sender.toBase58(), 30000, 0);
////
////			System.out.println(tx.json());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testTransfer () throws Exception {
		OntSdk ontSdk = getOntSdk();

		Account acct0 = new Account(Helper.hexToBytes(privatekey0), ontSdk.defaultSignScheme);
		Account acct1 = new Account(Helper.hexToBytes(privatekey1), ontSdk.defaultSignScheme);

		Object o = ontSdk.getConnect().getBalance(acct0.getAddressU160().toBase58());
		JSONObject balance0 = JSONObject.fromObject(o);
		System.out.println(balance0.toString());

		o = ontSdk.getConnect().getBalance(acct1.getAddressU160().toBase58());
		JSONObject balance1 = JSONObject.fromObject(o);
		System.out.println(balance1.toString());

		Transaction tx = ontSdk.nativevm().ont().makeTransfer(acct1.getAddressU160().toBase58(), acct0.getAddressU160().toBase58(),
				1890182, acct0.getAddressU160().toBase58(), ontSdk.DEFAULT_GAS_LIMIT, 500);
		ontSdk.signTx(tx, new Account[][]{{acct1},{acct0}});

		o = ontSdk.getConnect().sendRawTransactionSync(tx.toHexString());
		JSONObject rs = JSONObject.fromObject(o);
		System.out.println(rs.toString());

		o = ontSdk.getConnect().getBalance(acct0.getAddressU160().toBase58());
		JSONObject b0 = JSONObject.fromObject(o);
		System.out.println(b0.toString());

		o = ontSdk.getConnect().getBalance(acct1.getAddressU160().toBase58());
		JSONObject b1 = JSONObject.fromObject(o);
		System.out.println(b1.toString());
	}

	@Test
	public void withdraw () throws Exception {
		OntSdk ontSdk = getOntSdk();

		Account acct0 = new Account(Helper.hexToBytes(privatekey0), ontSdk.defaultSignScheme);
		Account acct1 = new Account(Helper.hexToBytes(privatekey1), ontSdk.defaultSignScheme);

		System.out.println("acct0 ong:"+ ontSdk.nativevm().ong().queryBalanceOf(acct0.getAddressU160().toBase58()));
		System.out.println("acct0 ont:"+ ontSdk.nativevm().ont().queryBalanceOf(acct0.getAddressU160().toBase58()));

		String uong = ontSdk.nativevm().ong().unboundOng(acct0.getAddressU160().toBase58());
		System.out.println("unbound ong:" + uong);

		String drawOng = ontSdk.nativevm().ong().withdrawOng(acct0, acct1.getAddressU160().toBase58(), 1000000L, acct0, ontSdk.DEFAULT_GAS_LIMIT, 500);
		System.out.println("draw ong:" + drawOng);

		System.out.println("acct0 ong:"+ ontSdk.nativevm().ong().queryBalanceOf(acct0.getAddressU160().toBase58()));
		System.out.println("acct0 ont:"+ ontSdk.nativevm().ont().queryBalanceOf(acct0.getAddressU160().toBase58()));
	}

}
