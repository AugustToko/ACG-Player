package top.geek_studio.chenlongcould.musicplayer.fragment;

import android.content.SharedPreferences;
import android.os.RemoteException;

import androidx.annotation.WorkerThread;

import java.util.Collections;
import java.util.Random;

import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.Values;
import top.geek_studio.chenlongcould.musicplayer.database.DataModel;

/**
 * @author : chenlongcould
 * @date : 2019/08/29/18
 */
abstract class BaseDetailFragment extends BaseFragment {
	@WorkerThread
	synchronized static void shuffleOrderListSync(boolean reset, final DataModel dataModel, final SharedPreferences preferences) {
		long seed;
		if (reset) {
			Data.sPlayOrderList.clear();
			Data.sPlayOrderList.addAll(dataModel.mMusicItems);
			seed = 0;
		} else {
			seed = new Random().nextLong();
			Collections.shuffle(Data.sPlayOrderList, new Random(seed));
			preferences.edit().putLong(Values.SharedPrefsTag.RANDOM_LIST_SEED, seed).apply();
		}

		try {
			Data.sMusicBinder.syncOderList(seed);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

}
