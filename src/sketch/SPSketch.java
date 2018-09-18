package sketch;

import org.voltdb.SQLStmt;
import org.voltdb.VoltProcedure;
import org.voltdb.VoltTable;

public class SPSketch extends VoltProcedure {
    public final SQLStmt query = new SQLStmt("SELECT * FROM T;");

    public VoltTable[] run(int param) {
        voltQueueSQL(query, param);
        return voltExecuteSQL(true);
    }
}
