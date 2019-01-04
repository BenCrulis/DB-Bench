import benchmark.API;
import benchmod.BenchMod;
import benchresult.ResultRow;
import concretebenchmods.DatabaseUtil;

import java.sql.*;

public class Main {


    public static final String RAW_CUSTOM_1 = "select REGION.R_NAME, PART.P_TYPE, sum(ORDERS.O_TOTALPRICE), avg(ORDERS.O_TOTALPRICE)\n" +
            "from ORDERS,\n" +
            "     CUSTOMER,\n" +
            "     NATION,\n" +
            "     REGION,\n" +
            "     LINEITEM,\n" +
            "     PART\n" +
            "where ORDERS.O_SHIPPRIORITY = 0\n" +
            "  and CUSTOMER.C_NATIONKEY = NATION.N_NATIONKEY\n" +
            "  and NATION.N_REGIONKEY = REGION.R_REGIONKEY\n" +
            "  and LINEITEM.L_PARTKEY = PART.P_PARTKEY\n" +
            "  and ORDERS.O_ORDERKEY = LINEITEM.L_ORDERKEY\n" +
            "  and C_CUSTKEY = O_CUSTKEY\n" +
            "group by REGION.R_NAME, PART.P_TYPE\n" +
            "having sum(ORDERS.O_TOTALPRICE) > 10000;";

    public static final String RAW_CUSTOM_2 = "select S.REG, S.p_type, S.p_name, M.ma\n" +
            "from (select REGION.r_name as REG, PART.p_name, PART.p_type,\n" +
            "sum(LINEITEM.l_quantity) as su\n" +
            "      from PART,\n" +
            "           CUSTOMER,\n" +
            "           ORDERS,\n" +
            "           NATION,\n" +
            "           REGION,\n" +
            "           LINEITEM\n" +
            "      where LINEITEM.l_shipmode <> 'MAIL'\n" +
            "        and (p_partkey = l_partkey and o_orderkey = l_orderkey and\n" +
            "             O_CUSTKEY = C_CUSTKEY and C_NATIONKEY = N_NATIONKEY and\n" +
            "             N_REGIONKEY = R_REGIONKEY)\n" +
            "        AND ORDERS.o_orderdate > MAKE_DATE(1996,1,1)\n" +
            "      group by REGION.r_name, PART.p_type, PART.p_name) as S\n" +
            "       join (select TMP.REG, TMP.p_type, max(TMP.su) as ma\n" +
            "             from (select r_name as REG, P_NAME, P_TYPE, sum(L_QUANTITY) as su\n" +
            "                   from PART,\n" +
            "                        CUSTOMER,\n" +
            "                        ORDERS,\n" +
            "                        NATION,\n" +
            "                        REGION,\n" +
            "                        LINEITEM\n" +
            "                   where l_shipmode <> 'MAIL'\n" +
            "                     AND o_orderdate > MAKE_DATE(1996,1,1)\n" +
            "                     AND (P_PARTKEY = L_PARTKEY and o_orderkey = l_orderkey and\n" +
            "                          O_CUSTKEY = C_CUSTKEY and C_NATIONKEY = N_NATIONKEY and\n" +
            "                          N_REGIONKEY = R_REGIONKEY)\n" +
            "                   group by r_name, p_type, p_name) as TMP\n" +
            "             group by TMP.REG, TMP.p_TYPE) as M on S.REG = M.REG\n" +
            "             and S.p_type = M.p_TYPE and S.su = M.ma;";

    public static final String TPCH_2 = "select s_acctbal, s_name, n_name, p_partkey, p_mfgr, s_address,s_phone, s_comment\n" +
            "from part, supplier, partsupp, nation, region\n" +
            "where p_partkey = ps_partkey\n" +
            "  and s_suppkey = ps_suppkey\n" +
            "  and p_size = 15\n" +
            "  and p_type like '%BRASS'\n" +
            "  and s_nationkey = n_nationkey\n" +
            "  and n_regionkey = r_regionkey\n" +
            "  and r_name = 'EUROPE'\n" +
            "  and ps_supplycost = (\n" +
            "  select min(ps_supplycost)\n" +
            "  from partsupp, supplier, nation, region\n" +
            "  where p_partkey = ps_partkey\n" +
            "    and s_suppkey = ps_suppkey\n" +
            "    and s_nationkey = n_nationkey\n" +
            "    and n_regionkey = r_regionkey\n" +
            "    and r_name = 'EUROPE'\n" +
            ")\n" +
            "order by s_acctbal desc,\n" +
            "         n_name,\n" +
            "         s_name,\n" +
            "         p_partkey;";

    public static final String TPCH_21 = "select s_name,\n" +
            "       count(*) as numwait\n" +
            "from supplier,\n" +
            "     lineitem l1,\n" +
            "     orders,\n" +
            "     nation\n" +
            "where s_suppkey = l1.l_suppkey\n" +
            "  and o_orderkey = l1.l_orderkey\n" +
            "  and o_orderstatus = 'F'\n" +
            "  and l1.l_receiptdate > l1.l_commitdate\n" +
            "  and exists(\n" +
            "    select *\n" +
            "    from lineitem l2\n" +
            "    where l2.l_orderkey = l1.l_orderkey\n" +
            "      and l2.l_suppkey <> l1.l_suppkey\n" +
            "  )\n" +
            "  and not exists(\n" +
            "    select *\n" +
            "    from lineitem l3\n" +
            "    where l3.l_orderkey = l1.l_orderkey\n" +
            "      and l3.l_suppkey <> l1.l_suppkey\n" +
            "      and l3.l_receiptdate > l3.l_commitdate\n" +
            "  )\n" +
            "  and s_nationkey = n_nationkey\n" +
            "  and n_name = 'SAUDI ARABIA'\n" +
            "group by s_name\n" +
            "order by numwait desc,\n" +
            "         s_name;";


    public static final BenchMod.Tag<Connection, Void> custom1 = API.tag("query", "custom1",DatabaseUtil.query(RAW_CUSTOM_1));
    public static final BenchMod.Tag<Connection, Void> custom2 = API.tag("query", "custom2",DatabaseUtil.query(RAW_CUSTOM_2));
    public static final BenchMod.Tag<Connection, Void> tpch2 = API.tag("query", "tpch2",DatabaseUtil.query(TPCH_2));
    public static final BenchMod.Tag<Connection, Void> tpch21 = API.tag("query", "tpch21",DatabaseUtil.query(TPCH_21));


    public static final BenchMod.Sequence<Connection> allQueries = API.sequence(custom1, custom2, tpch2, tpch21);


    public static void main(String[] args) throws SQLException {


        String host = args[0];
        String user = args[1];
        String password = args[2];

        try {
            Class.forName(DatabaseUtil.POSTGRES_DRIVER_NAME);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.err.println("Driver "+DatabaseUtil.POSTGRES_DRIVER_NAME+" not found.");
        }


        BenchMod<Void,Void> benchMod = API.asContext(DatabaseUtil.postgresContext(host,"tpch",user, password),
                API.repeat(5,"iteration",allQueries));

        for (ResultRow resultRow :
                API.iterate(benchMod)) {
            System.out.println(resultRow);
        }

    }

}
