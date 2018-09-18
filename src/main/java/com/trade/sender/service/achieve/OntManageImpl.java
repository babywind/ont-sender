package com.trade.sender.service.achieve;

import com.github.ontio.OntSdk;
import com.github.ontio.account.Account;
import com.github.ontio.common.Helper;
import com.github.ontio.core.transaction.Transaction;
import com.quqian.framework.service.ServiceFactory;
import com.quqian.framework.service.ServiceResource;
import com.quqian.framework.service.query.ArrayParser;
import com.quqian.framework.service.query.ItemParser;
import com.quqian.p2p.common.enums.IsPass;
import com.quqian.p2p.common.enums.XlbType;
import com.quqian.p2p.variables.P2PConst;
import com.quqian.util.MyCrypt;
import com.quqian.util.StringHelper;
import com.quqian.util.parser.EnumParser;
import com.trade.sender.entity.BEntity;
import com.trade.sender.entity.Lsqbdz;
import com.trade.sender.entity.OntEntity;
import com.trade.sender.entity.OutWalletEntity;
import com.trade.sender.service.OntManage;
import net.sf.json.JSONObject;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class OntManageImpl extends AbstractOntService implements OntManage {
	private final static int CREATE_NUM = 100;
	private final static long GAS_PRICE = 500L;
	private final static String bjc = "ONT";

	enum TRADE { TRANS_IN,TRANS_OUT }

	private final int BID = selectId(bjc);

	private OntSdk ontSdk;

	private OntManageImpl(ServiceResource serviceResource) {
		super(serviceResource);

		this.init();
	}

	private void init() {
		try {
			OntEntity oe = this.getOntInfo();

			ontSdk = OntSdk.getInstance();
			ontSdk.setRpc(oe.ip);

			ontSdk.setDefaultConnect(ontSdk.getRpc());
		} catch (Throwable throwable) {
			throwable.printStackTrace();
		}
	}

	private OntEntity getOntInfo() throws Exception {
		return select(getConnection(P2PConst.DB_USER), new ItemParser<OntEntity>() {
			@Override
			public OntEntity parse(ResultSet re) throws SQLException {
				OntEntity b = new OntEntity();
				while (re.next()) {
					b.r_qbdz = re.getString(1);
					b.r_sy = re.getString(2);
					b.l_qbdz = re.getString(3);
					b.count = re.getBigDecimal(4);
					b.is = EnumParser.parse(IsPass.class, re.getString(5));
					b.ip = re.getString(6);
				}
				return b;
			}
		}, "SELECT F12,F13,F14,F16,F15,F18 FROM T6013 WHERE F01=?", BID);
	}

	private OutWalletEntity getOutWalet() throws Exception {
		return select(getConnection(P2PConst.DB_CONSOLE), (re) -> {
					OutWalletEntity t = new OutWalletEntity();

					if (re.next()) {
						t.id = re.getInt(1);
						t.bid = re.getInt(2);
						t.address = re.getString(3);
						t.privateKey = re.getString(4);
						t.ip = re.getString(5);
						t.port = re.getString(6);
						t.serverName = re.getString(7);
						t.serverPasswd = re.getString(8);
					}

					return t;
				},
				"SELECT F01,F02,F03,F04,F05,F06,F07,F08 FROM T7103 WHERE F02 = ?", BID
		);
	}

	@Override
	public void ont_rqb(Lsqbdz l) throws Exception {
		String hash = "";
		String rs = "";
		long transAmount = 0L;

		try {
			// 获取ONT参数信息
			OntEntity ont = this.getOntInfo();

			//转出账户
			Account sendAcct = new Account(Helper.hexToBytes(MyCrypt.myDecode(l.sy)), ontSdk.defaultSignScheme);
			//转出地址
			String sendAddr = sendAcct.getAddressU160().toBase58();

			// 转入账户
			Account recvAcct = new Account(Helper.hexToBytes(MyCrypt.myDecode(ont.r_sy)), ontSdk.defaultSignScheme);
			// 转入地址
			String recvAddr = ont.r_qbdz;

			// 查询用户钱包信息
			long ontMoney = ontSdk.nativevm().ont().queryBalanceOf(sendAddr);

			if (ontMoney > 0) {
				//转账金额
				transAmount = ontMoney;
				// 转账(1.构造转账事务 2.账号签名)
				Transaction tx = ontSdk.nativevm().ont().makeTransfer(sendAddr, recvAddr, transAmount, recvAddr, ontSdk.DEFAULT_GAS_LIMIT, GAS_PRICE);
				ontSdk.signTx(tx, new Account[][]{{sendAcct}, {recvAcct}});

				// 同步转账交易
				Object o = ontSdk.getConnect().syncSendRawTransaction(tx.toHexString());

				JSONObject result = JSONObject.fromObject(o);

				hash = result.getString("TxHash");
				rs = result.toString();
			} else {
				throw new Exception("账户余额不足，转账失败！");
			}
		} catch (Exception e) {
			transAmount = 0L;
			rs = e.getMessage();
		}

		// 更新转账记录状态
		execute(getConnection(P2PConst.DB_USER),
				"UPDATE  T6012_3 SET F05=?,F06=CURRENT_TIMESTAMP(),F08=?,F09=? WHERE F01=?",
				IsPass.S, hash, GAS_PRICE, l.id);
		execute(getConnection(P2PConst.DB_USER),
				"INSERT INTO T6012_4 (F01, F03) VALUES (?, ?) ON DUPLICATE KEY UPDATE F03 = ? ",
				l.id, rs, rs);

		// 资产更新
		if (transAmount > 0) {
			// 更新用户资产
			updateUserAsset(l.userid, BID, transAmount, 0, TRADE.TRANS_IN);

			// 更新平台资产？
		}
	}

	@Override
	public void ont_lqb() throws Throwable {
		// 获取ONT参数信息
		OntEntity ont = this.getOntInfo();
		if (IsPass.S == ont.is && !StringHelper.isEmpty(ont.l_qbdz)) {
			Account hotAcct = new Account(Helper.hexToBytes(MyCrypt.myDecode(ont.r_sy)), ontSdk.defaultSignScheme);

			String sendAddr = hotAcct.getAddressU160().toBase58();
			String recvAddr = ont.l_qbdz;

			//查询余额
			long transFee = ontSdk.nativevm().ont().queryBalanceOf(sendAddr);

			if (transFee > ont.count.longValue()) {
				Transaction tx = ontSdk.nativevm().ont().makeTransfer(sendAddr, recvAddr, transFee, sendAddr, ontSdk.DEFAULT_GAS_LIMIT, GAS_PRICE);
				ontSdk.signTx(tx, new Account[][]{{hotAcct}});

				ontSdk.getConnect().sendRawTransaction(tx.toHexString());
			}
		}
	}

	@Override
	public void ontTransOut(BEntity bEntity) throws Exception {
		String state = "ZCSB";
		String hash = "";
		// 获取平台对应提币钱包信息
		OutWalletEntity outWallet = this.getOutWalet();

		// 平台提币钱包余额是否足够
		Account platAccount = new Account(Helper.hexToBytes(MyCrypt.myDecode(outWallet.privateKey)), ontSdk.defaultSignScheme);

		JSONObject balance = JSONObject.fromObject(ontSdk.getConnect().getBalance(platAccount.getAddressU160().toBase58()));

		long ontBalance = balance.getInt("ont");
		//long ongBalance = balance.getInt("ong");

		long transAmount = bEntity.amount.longValue();

		// 判断平台提币钱包余额是否充足
		if (ontBalance >= transAmount ) {
			try {
				// 操作转账
				String sendAddr = platAccount.getAddressU160().toBase58();
				String recvAddr = bEntity.address;

				Transaction tx = ontSdk.nativevm().ont().makeTransfer(sendAddr, recvAddr, transAmount, sendAddr, ontSdk.DEFAULT_GAS_LIMIT, GAS_PRICE);
				ontSdk.signTx(tx, new Account[][]{{platAccount}});

				Object object = ontSdk.getConnect().sendRawTransaction(tx.toHexString());

				JSONObject result = JSONObject.fromObject(object);
				hash = result.getString("TxHash");

				// 转币成功
				state = "ZCCG";
				// 更新减少用户资产(T6025)
				updateUserAsset(bEntity.userId, bEntity.bid, transAmount, bEntity.charge.longValue(), TRADE.TRANS_OUT);

				// 更新平台资产(T7015) ?

			} catch (Exception e) {
				// 失败报错
				hash = e.getMessage().substring(1,200);
			}
		}

		// 更新提币记录
		execute(getConnection(P2PConst.DB_USER),
				"UPDATE T6028 SET F10 = ?, F13 = ?, F14 = CURRENT_TIMESTAMP(), F16 = ? WHERE F01 = ? ",
				state, bEntity.userId, hash, bEntity.id
		);
	}

	@Override
	public void createNewOntAccount() throws Exception {
		// 获取未使用的ont账户信息
		int cnt = selectInt(P2PConst.DB_USER, "SELECT COUNT(F01) FROM T6012_" + bjc + " WHERE F02 IS NULL AND F05 = 'F' ");

		if (cnt < CREATE_NUM) {
			for (int i = 0; i < CREATE_NUM; i++) {
				Account newAcct = new Account(ontSdk.defaultSignScheme);

				String address = newAcct.getAddressU160().toBase58();
				String privateKey = MyCrypt.myEncode(Helper.toHexString(newAcct.serializePrivateKey()));

				execute(getConnection(P2PConst.DB_USER),
						"INSERT INTO T6012_" + bjc + " (F03,F04,F05,F06) VALUES (?, ?, 'F', CURRENT_TIMESTAMP())",
						address, privateKey
				);
			}
		}
	}

	@Override
	public Lsqbdz[] getTranscationInfos() throws Throwable {
		return selectAll(getConnection(P2PConst.DB_USER),
			new ArrayParser<Lsqbdz>() {
				ArrayList<Lsqbdz> list = new ArrayList<>();

				@Override
				public Lsqbdz[] parse(ResultSet re) throws SQLException {
					while (re.next()) {
						Lsqbdz s = new Lsqbdz();
						s.id = re.getLong(1);
						s.zrje = re.getBigDecimal(2);
						s.qbdz = re.getString(3);
						s.sy = re.getString(4);
						s.T6012_id = re.getLong(5);
						s.userid = re.getLong(6);
						list.add(s);
					}
					return list.size() == 0 ? null : list.toArray(new Lsqbdz[list.size()]);
				}
			},
			"SELECT T6012_3.F01,T6012_3.F04,T6.F03,T6.F04,T6.F01,T6.F02 FROM T6012_3 LEFT JOIN T6012_"
			+ bjc + " AS T6 ON T6012_3.F07 = T6.F01 WHERE T6012_3.F05=? AND T6012_3.F10=? ",
			IsPass.F, BID
		);
	}

	@Override
	public BEntity[] getTransOutInfos() throws Exception {
		return selectAll(getConnection(P2PConst.DB_USER),
			(re) -> {
				ArrayList<BEntity> list = new ArrayList<>();
				while (re.next()) {
					BEntity e = new BEntity();

					e.id = re.getLong(1);
					e.userId = re.getLong(2);
					e.bid = re.getInt(3);
					e.amount = re.getBigDecimal(4);
					e.charge = re.getBigDecimal(5);
					e.address = re.getString(6);

					list.add(e);
				}

				return list.size() == 0 ? null : list.toArray(new BEntity[list.size()]);
			},
			"SELECT A.F01, A.F02 USER_ID, A.F03 BID A.F05 TRANS_AMOUNT, A.F06 CHARGE, B.F05 ADDRESS "
			+ "FROM T6028 A, T6012 B WHERE A.F02 = B.F02 AND A.F03 = B.F03 AND A.F04 = B.F01 "
			+ "AND A.F10 = 'SHTG' AND A.F08 = 'F' AND A.F02 = ? ", BID
		);
	}

	/**
	 * 更新并记录用户资产变动
	 * @param userId 用户ID
	 * @param bid 数字币ID
	 * @param amount 金额
	 * @param charge 手续费
	 * @param transTag TRANS_IN：转入平台(充值) TRANS_OUT：转出平台(提现)
	 * @throws Exception SQL EXCEPTION
	 */
	private void updateUserAsset(long userId, int bid, long amount, long charge, TRADE transTag) throws Exception {
		BigDecimal userBalance = selectBigDecimal(getConnection(P2PConst.DB_USER),
				"SELECT F04 + F05 FROM T6025 WHERE F02 = ? AND F03 = ? FOR UPDATE ", userId, bid
		);

		String tradeTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

		if (transTag == TRADE.TRANS_IN) {
			// 更新用户资产
			execute(getConnection(P2PConst.DB_USER),
					"INSERT INTO T6025 (F02, F03, F04) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE F04 = F04 + ? ",
					userId, bid, amount, amount
			);

			// 用户虚拟币交易记
			execute(getConnection(P2PConst.DB_USER),
					"INSERT INTO T6026 SET F02=?,F03=?,F04=?,F05=CURRENT_TIMESTAMP(),F06=?,F08=?,F09=?,F10=? ",
					userId, bid, XlbType.ZR, amount, "", userBalance, userBalance.add(new BigDecimal(amount))
			);

			// 转入到账记录
			execute(getConnection(P2PConst.DB_USER),
					"INSERT INTO T6027 SET F02=?,F03=?,F04=?,F05=?,F06=?,F07=CURRENT_TIMESTAMP(),F08=?,F09=?,F10=?",
					userId, bid, amount, amount, "", "", IsPass.S, ""
			);

			String content = String.format("尊敬的用户，您于%s转入%d枚%s，感谢您的使用。", tradeTime, amount, bjc);
			sms(userId, "转入ONT", content);
		} else {
			// 更新用户资产
			execute(getConnection(P2PConst.DB_USER),
				"UPDATE T6025 SET F05 = F05 - ? WHERE F02 = ? AND F03 = ?",
				amount + charge, userId, bid
			);

			// 用户虚拟币交易记(提币数)
			execute(getConnection(P2PConst.DB_USER),
					"INSERT INTO T6026 SET F02=?, F03=?, F04=?, F05=CURRENT_TIMESTAMP(), F06=?, F08=?, F09=?, F10=? ",
					userId, bid, XlbType.ZC, amount, "", userBalance, userBalance.subtract(new BigDecimal(amount))
			);

			// 用户虚拟币交易记(提币手续费)
			if (charge > 0) {
				execute(getConnection(P2PConst.DB_USER),
						"INSERT INTO T6026 SET F02=?, F03=?, F04=?, F05=CURRENT_TIMESTAMP(), F06=?, F08=?, F09=?, F10=? ",
						userId, bid, XlbType.TBSXF, charge, "", userBalance, userBalance.subtract(new BigDecimal(charge))
				);
			}

			String content = String.format("尊敬的用户，您于%s转出%d枚%s，感谢您的使用。", tradeTime, amount, bjc);
			sms(userId, "提币成功", content);
		}
	}

	private void sms (long userId, String title, String content) throws Exception {
		execute(getConnection(P2PConst.DB_USER),
				"INSERT INTO T6100 SET F02=?,F03=?,F04=?,F05=?,F06=CURRENT_TIMESTAMP()",
				userId, title, content, "WD");
	}

	public static class OntManageFactory implements ServiceFactory<OntManage> {
		@Override
		public OntManage newInstance(ServiceResource serviceResource) {
			return new OntManageImpl(serviceResource);
		}
	}
}