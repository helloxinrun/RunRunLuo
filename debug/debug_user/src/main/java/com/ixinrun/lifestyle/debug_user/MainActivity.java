package com.ixinrun.lifestyle.debug_user;

import androidx.fragment.app.Fragment;

import com.ixinrun.lifestyle.common.debug.BaseModuleDebugActivity;
import com.ixinrun.lifestyle.module_user.main.MainUserFrag;

public class MainActivity extends BaseModuleDebugActivity {

    @Override
    protected Fragment getFragment() {
        return MainUserFrag.newInstance();
    }
}
