package top.geek_studio.chenlongcould.geeklibrary;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.NonNull;

/**
 * @author chenlongcould
 */
public class DialogUtil {

    /**
     * @param context context
     * @param aTitle  theTitle
     */
	public static androidx.appcompat.app.AlertDialog getLoadingDialog(@NonNull final Context context
			, @NonNull final String... aTitle) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
        final View loadView = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null);
        // TODO: 2019/1/7 custom Theme loading animation
        builder.setView(loadView);
        builder.setTitle(aTitle.length == 0 ? "Loading..." : aTitle[0]);
        builder.setCancelable(false);
        return builder.create();
    }

}
