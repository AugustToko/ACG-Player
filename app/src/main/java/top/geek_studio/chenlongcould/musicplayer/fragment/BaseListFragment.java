package top.geek_studio.chenlongcould.musicplayer.fragment;

import androidx.annotation.Nullable;
import top.geek_studio.chenlongcould.musicplayer.model.Item;

/**
 * BaseListFragment
 *
 * @author : chenlongcould
 * @date : 2019/06/08/18
 */
public abstract class BaseListFragment extends BaseFragment {
	public abstract boolean removeItem(@Nullable Item item);

	public abstract boolean addItem(@Nullable Item item);
}
