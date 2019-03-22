package net.jpralves.slandroid;

// imported from: http://andydennie.wordpress.com/2011/09/26/strictmode-wrapper/

//the call below to setContentView will trigger StrictMode disk read and disk write
//violations because the WebView accesses a WebViewDatabase. So, bracket the call
//to setContentView with calls to first permit those actions, and then afterward,
//restore the policy permissions that were previously in place.
/*
 StrictModeWrapper strictMode = VersionedStrictModeWrapper.getInstance();
 ThreadPolicyWrapper origPolicy = strictMode.allowThreadDiskReads();
 strictMode.allowThreadDiskWrites();

 setContentView(R.layout.my_web_view); // creates a WebView

 strictMode.setThreadPolicy(origPolicy);
 */

import net.jpralves.slandroid.VersionedStrictModeWrapper.StrictModeWrapper.ThreadPolicyWrapper;
import net.jpralves.slandroid.VersionedStrictModeWrapper.StrictModeWrapper.VmPolicyWrapper;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.os.StrictMode.VmPolicy;

/**
 * VersionedStrictModeWrapper Object.
 *
 * <P>
 * Version-tolerant StrictMode wrapper for Android.
 *
 * @author Andy Dennie and Manfred Moser
 * @version 1.0
 */
public class VersionedStrictModeWrapper {

	public interface StrictModeWrapper {
		void init(Context context);

		ThreadPolicyWrapper allowThreadDiskReads();

		ThreadPolicyWrapper allowThreadDiskWrites();

		ThreadPolicyWrapper allowThreadNetwork();

		void setThreadPolicy(ThreadPolicyWrapper wrapper);

		void setVmPolicy(VmPolicyWrapper wrapper);

		interface ThreadPolicyWrapper {
		}

		interface VmPolicyWrapper {
		}
	}

	static public StrictModeWrapper getInstance() {
		StrictModeWrapper wrapper = null;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			wrapper = new GingerbreadStrictModeWrapper();
		} else {
			wrapper = new NoopStrictModeWrapper();
		}
		return wrapper;
	}

	static class NoopStrictModeWrapper implements StrictModeWrapper {
		public void init(final Context context) {
		}

		public ThreadPolicyWrapper allowThreadDiskReads() {
			return null;
		}

		public ThreadPolicyWrapper allowThreadDiskWrites() {
			return null;
		}

		public ThreadPolicyWrapper allowThreadNetwork() {
			return null;
		}

		public void setThreadPolicy(final ThreadPolicyWrapper wrapper) {
		}

        public void setVmPolicy(final VmPolicyWrapper wrapper) {
		}

    }

	static class GingerbreadStrictModeWrapper implements StrictModeWrapper {

		public void init(final Context context) {
			if ((context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
				StrictMode.enableDefaults();
			}
		}

		public ThreadPolicyWrapper allowThreadDiskReads() {
			return new GingerbreadThreadPolicyWrapper(StrictMode.allowThreadDiskReads());
		}

		public ThreadPolicyWrapper allowThreadDiskWrites() {
			return new GingerbreadThreadPolicyWrapper(StrictMode.allowThreadDiskWrites());
		}

		public ThreadPolicyWrapper allowThreadNetwork() {
			ThreadPolicy origPolicy = StrictMode.getThreadPolicy();
			ThreadPolicy newPolicy = new ThreadPolicy.Builder(origPolicy).permitNetwork().build();
			StrictMode.setThreadPolicy(newPolicy);
			return new GingerbreadThreadPolicyWrapper(origPolicy);
		}

		public void setThreadPolicy(final ThreadPolicyWrapper wrapper) {
			StrictMode.setThreadPolicy(((GingerbreadThreadPolicyWrapper) wrapper).getPolicy());
		}

		public void setVmPolicy(final VmPolicyWrapper wrapper) {
			StrictMode.setVmPolicy(((GingerbreadVmPolicyWrapper) wrapper).getPolicy());
		}
	}

	static class GingerbreadThreadPolicyWrapper implements ThreadPolicyWrapper {
		private final ThreadPolicy mPolicy;

		public GingerbreadThreadPolicyWrapper(final StrictMode.ThreadPolicy policy) {
			mPolicy = policy;
		}

		public StrictMode.ThreadPolicy getPolicy() {
			return mPolicy;
		}
	}

	static class GingerbreadVmPolicyWrapper implements VmPolicyWrapper {
		private final VmPolicy mPolicy;

		public GingerbreadVmPolicyWrapper(final StrictMode.VmPolicy policy) {
			mPolicy = policy;
		}

		public StrictMode.VmPolicy getPolicy() {
			return mPolicy;
		}
	}
}
