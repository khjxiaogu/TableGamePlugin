import java.io.File;

import com.khjxiaogu.TableGames.data.CachedSet;
import com.khjxiaogu.TableGames.platform.GlobalMain;
import com.khjxiaogu.TableGames.utils.SimpleGameLogger;

public class DataSetTest {
	static {
		GlobalMain.setLogger(new SimpleGameLogger("test"));
	}
	static CachedSet cds=new CachedSet(new File("test.db"), "tt");
	public static void main(String[] args) {

	}

}
