import org.voltdb.SQLStmt;
import org.voltdb.VoltProcedure;
import org.voltdb.VoltTable;

public class CheckAndUpdate extends VoltProcedure {

    /**
     * 检查指定账号对应的版本号
     */
    public final SQLStmt checkVersion =
            new SQLStmt("SELECT version FROM t WHERE accid = ?");

    /**
     * 将指定账号对应的版本号递增 1
     */
    public final SQLStmt updateVersion =
            new SQLStmt("UPDATE t SET version = version + 1 WHERE accid = ?");

    /**
     * 存储过程运行入口
     * @param account1 第一个 account number
     * @param localVersion1 第一个待比对的版本号
     * @param account2 第二个 account number
     * @param localVersion2 第二个待比对的版本号
     * @return 更新结果
     */
    public long run(long account1, long localVersion1, long account2, long localVersion2) {
        long[] accounts = new long[] {account1, account2};
        long[] localVersions = new long[] {localVersion1, localVersion2};
        assert(accounts.length == localVersions.length);

        long updatedCount = 0;
        for (int i = 0; i < accounts.length; i++) {
            voltQueueSQL(checkVersion, accounts[i]);
            VoltTable vt = voltExecuteSQL()[0];
            // If the vt is empty, it could be that the account we are looking for is not on this partition.
            // It will be handled in the other partition.
            if (vt.advanceRow()) {
                long serverVersion = vt.getLong(0);
                if (serverVersion != localVersions[i]) {
                    throw new VoltAbortException("Version mismatch, abort the transaction.");
                } else {
                    voltQueueSQL(updateVersion, accounts[i]);
                    updatedCount += voltExecuteSQL()[0].asScalarLong();
                }
            }
        }
        return updatedCount;
    }
}
