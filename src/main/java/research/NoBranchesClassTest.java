package research;

import org.junit.Before;
import org.junit.Test;

public class NoBranchesClassTest {

    private NoBranchesClass noBranchesClass;

    @Before
    public void setUp() {
        this.noBranchesClass = new NoBranchesClass();
    }

    @Test
    public void test01() {
        noBranchesClass.method01();
    }

    @Test
    public void test02() {
        noBranchesClass.method02();
    }

    @Test
    public void test03() {
        noBranchesClass.method03();
    }

}