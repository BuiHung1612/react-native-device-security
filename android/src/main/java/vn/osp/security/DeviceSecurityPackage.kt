package vn.osp.security

import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.module.model.ReactModuleInfo
import com.facebook.react.module.model.ReactModuleInfoProvider
import com.facebook.react.uimanager.ViewManager

/**
 * React Native package for device security module
 */
class DeviceSecurityPackage : ReactPackage {

    override fun createNativeModules(reactContext: ReactApplicationContext): List<NativeModule> {
        return listOf(DeviceSecurityModule(reactContext))
    }

    override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager<*, *>> {
        return emptyList()
    }

    override fun getModuleInfoProvider(): ReactModuleInfoProvider {
        return ReactModuleInfoProvider {
            val moduleInfo: MutableMap<String, ReactModuleInfo> = java.util.HashMap()

            val methods = listOf(
                "isRooted",
                "isRootedWithDetails",
                "hasFrida",
                "hasXposed",
                "hasMagisk",
                "isDebuggable",
                "isEmulator",
                "getSecurityStatus",
                "isDeviceSecure",
                "blockOnSecurityThreat"
            )

            val constants = listOf("NAME", "NATIVE_LIBRARY_LOADED")

            moduleInfo["DeviceSecurity"] = ReactModuleInfo(
                "DeviceSecurity",
                "DeviceSecurity",
                false,  // isTurboModule
                false,  // isCxxModule
                true,   // canOverrideExistingModule
                methods,
                constants,
                false,  // supportsEventEmitter
                false,  // needsDispatchEvent
                true    // hasConstants
            )

            moduleInfo
        }
    }
}
