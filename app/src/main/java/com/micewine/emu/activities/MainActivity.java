package com.micewine.emu.activities;

import static com.micewine.emu.activities.GeneralSettingsActivity.BOX64_LOG;
import static com.micewine.emu.activities.GeneralSettingsActivity.BOX64_LOG_DEFAULT_VALUE;
import static com.micewine.emu.activities.GeneralSettingsActivity.BOX64_NOSIGILL;
import static com.micewine.emu.activities.GeneralSettingsActivity.BOX64_NOSIGILL_DEFAULT_VALUE;
import static com.micewine.emu.activities.GeneralSettingsActivity.BOX64_NOSIGSEGV;
import static com.micewine.emu.activities.GeneralSettingsActivity.BOX64_NOSIGSEGV_DEFAULT_VALUE;
import static com.micewine.emu.activities.GeneralSettingsActivity.BOX64_SHOWBT;
import static com.micewine.emu.activities.GeneralSettingsActivity.BOX64_SHOWBT_DEFAULT_VALUE;
import static com.micewine.emu.activities.GeneralSettingsActivity.BOX64_SHOWSEGV;
import static com.micewine.emu.activities.GeneralSettingsActivity.BOX64_SHOWSEGV_DEFAULT_VALUE;
import static com.micewine.emu.activities.GeneralSettingsActivity.ENABLE_DRI3;
import static com.micewine.emu.activities.GeneralSettingsActivity.ENABLE_DRI3_DEFAULT_VALUE;
import static com.micewine.emu.activities.GeneralSettingsActivity.ENABLE_MANGOHUD;
import static com.micewine.emu.activities.GeneralSettingsActivity.ENABLE_MANGOHUD_DEFAULT_VALUE;
import static com.micewine.emu.activities.GeneralSettingsActivity.FPS_LIMIT;
import static com.micewine.emu.activities.GeneralSettingsActivity.PA_SINK;
import static com.micewine.emu.activities.GeneralSettingsActivity.PA_SINK_DEFAULT_VALUE;
import static com.micewine.emu.activities.GeneralSettingsActivity.SELECTED_BOX64;
import static com.micewine.emu.activities.GeneralSettingsActivity.SELECTED_CORE;
import static com.micewine.emu.activities.GeneralSettingsActivity.SELECTED_DXVK_HUD_PRESET;
import static com.micewine.emu.activities.GeneralSettingsActivity.SELECTED_DXVK_HUD_PRESET_DEFAULT_VALUE;
import static com.micewine.emu.activities.GeneralSettingsActivity.SELECTED_GL_PROFILE;
import static com.micewine.emu.activities.GeneralSettingsActivity.SELECTED_GL_PROFILE_DEFAULT_VALUE;
import static com.micewine.emu.activities.GeneralSettingsActivity.SELECTED_MESA_VK_WSI_PRESENT_MODE;
import static com.micewine.emu.activities.GeneralSettingsActivity.SELECTED_MESA_VK_WSI_PRESENT_MODE_DEFAULT_VALUE;
import static com.micewine.emu.activities.GeneralSettingsActivity.SELECTED_TU_DEBUG_PRESET;
import static com.micewine.emu.activities.GeneralSettingsActivity.SELECTED_TU_DEBUG_PRESET_DEFAULT_VALUE;
import static com.micewine.emu.activities.GeneralSettingsActivity.SELECTED_VULKAN_DRIVER;
import static com.micewine.emu.activities.GeneralSettingsActivity.WINE_DPI;
import static com.micewine.emu.activities.GeneralSettingsActivity.WINE_DPI_APPLIED;
import static com.micewine.emu.activities.GeneralSettingsActivity.WINE_DPI_APPLIED_DEFAULT_VALUE;
import static com.micewine.emu.activities.GeneralSettingsActivity.WINE_DPI_DEFAULT_VALUE;
import static com.micewine.emu.activities.GeneralSettingsActivity.WINE_LOG_LEVEL;
import static com.micewine.emu.activities.GeneralSettingsActivity.WINE_LOG_LEVEL_DEFAULT_VALUE;
import static com.micewine.emu.activities.PresetManagerActivity.SELECTED_BOX64_PRESET;
import static com.micewine.emu.activities.RatManagerActivity.generateICDFile;
import static com.micewine.emu.activities.RatManagerActivity.generateMangoHUDConfFile;
import static com.micewine.emu.activities.WelcomeActivity.finishedWelcomeScreen;
import static com.micewine.emu.adapters.AdapterGame.selectedGameName;
import static com.micewine.emu.adapters.AdapterRatPackage.BOX64;
import static com.micewine.emu.adapters.AdapterRatPackage.CORE;
import static com.micewine.emu.adapters.AdapterRatPackage.DXVK;
import static com.micewine.emu.adapters.AdapterRatPackage.VKD3D;
import static com.micewine.emu.adapters.AdapterRatPackage.VK_DRIVER;
import static com.micewine.emu.adapters.AdapterRatPackage.WINE;
import static com.micewine.emu.adapters.AdapterRatPackage.WINED3D;
import static com.micewine.emu.controller.ControllerUtils.connectedPhysicalControllers;
import static com.micewine.emu.controller.ControllerUtils.disconnectController;
import static com.micewine.emu.controller.ControllerUtils.prepareControllersMappings;
import static com.micewine.emu.core.EnvVars.getEnv;
import static com.micewine.emu.core.RatPackageManager.checkPackageInstalled;
import static com.micewine.emu.core.RatPackageManager.getPackageById;
import static com.micewine.emu.core.RatPackageManager.haveAnyPackageByCategory;
import static com.micewine.emu.core.RatPackageManager.installADToolsDriver;
import static com.micewine.emu.core.RatPackageManager.installRat;
import static com.micewine.emu.core.RatPackageManager.installablePackagesCategories;
import static com.micewine.emu.core.RatPackageManager.listRatPackages;
import static com.micewine.emu.core.RatPackageManager.listRatPackagesId;
import static com.micewine.emu.core.ShellLoader.runCommand;
import static com.micewine.emu.core.ShellLoader.runCommandWithOutput;
import static com.micewine.emu.core.WineWrapper.getCpuHexMask;
import static com.micewine.emu.core.WineWrapper.getSanitizedPath;
import static com.micewine.emu.core.WineWrapper.getUnixPath;
import static com.micewine.emu.fragments.AskInstallPackageFragment.ADTOOLS_DRIVER_PACKAGE;
import static com.micewine.emu.fragments.AskInstallPackageFragment.MWP_PRESET_PACKAGE;
import static com.micewine.emu.fragments.AskInstallPackageFragment.RAT_PACKAGE;
import static com.micewine.emu.fragments.AskInstallPackageFragment.adToolsDriverCandidate;
import static com.micewine.emu.fragments.AskInstallPackageFragment.mwpPresetCandidate;
import static com.micewine.emu.fragments.AskInstallPackageFragment.ratCandidate;
import static com.micewine.emu.fragments.Box64PresetManagerFragment.getBox64AlignedAtomics;
import static com.micewine.emu.fragments.Box64PresetManagerFragment.getBox64Avx;
import static com.micewine.emu.fragments.Box64PresetManagerFragment.getBox64BigBlock;
import static com.micewine.emu.fragments.Box64PresetManagerFragment.getBox64CallRet;
import static com.micewine.emu.fragments.Box64PresetManagerFragment.getBox64DF;
import static com.micewine.emu.fragments.Box64PresetManagerFragment.getBox64Dirty;
import static com.micewine.emu.fragments.Box64PresetManagerFragment.getBox64FastNan;
import static com.micewine.emu.fragments.Box64PresetManagerFragment.getBox64FastRound;
import static com.micewine.emu.fragments.Box64PresetManagerFragment.getBox64Forward;
import static com.micewine.emu.fragments.Box64PresetManagerFragment.getBox64MMap32;
import static com.micewine.emu.fragments.Box64PresetManagerFragment.getBox64NativeFlags;
import static com.micewine.emu.fragments.Box64PresetManagerFragment.getBox64Pause;
import static com.micewine.emu.fragments.Box64PresetManagerFragment.getBox64SafeFlags;
import static com.micewine.emu.fragments.Box64PresetManagerFragment.getBox64Sse42;
import static com.micewine.emu.fragments.Box64PresetManagerFragment.getBox64StrongMem;
import static com.micewine.emu.fragments.Box64PresetManagerFragment.getBox64Wait;
import static com.micewine.emu.fragments.Box64PresetManagerFragment.getBox64WeakBarrier;
import static com.micewine.emu.fragments.Box64PresetManagerFragment.getBox64X87Double;
import static com.micewine.emu.fragments.ControllerSettingsFragment.ACTION_UPDATE_CONTROLLERS_STATUS;
import static com.micewine.emu.fragments.CreatePresetFragment.BOX64_PRESET;
import static com.micewine.emu.fragments.CreatePresetFragment.CONTROLLER_PRESET;
import static com.micewine.emu.fragments.CreatePresetFragment.VIRTUAL_CONTROLLER_PRESET;
import static com.micewine.emu.fragments.DebugSettingsFragment.availableCPUs;
import static com.micewine.emu.fragments.EditGamePreferencesFragment.FILE_MANAGER_START_PREFERENCES;
import static com.micewine.emu.fragments.FileManagerFragment.refreshFiles;
import static com.micewine.emu.fragments.FloatingFileManagerFragment.OPERATION_SELECT_EXE;
import static com.micewine.emu.fragments.FloatingFileManagerFragment.OPERATION_SELECT_ICON;
import static com.micewine.emu.fragments.ShortcutsFragment.ADRENO_TOOLS_DRIVER;
import static com.micewine.emu.fragments.ShortcutsFragment.MESA_DRIVER;
import static com.micewine.emu.fragments.ShortcutsFragment.addGameToList;
import static com.micewine.emu.fragments.ShortcutsFragment.putExeArguments;
import com.micewine.emu.adapters.AdapterGame;
import com.micewine.emu.adapters.AdapterEnvVar;
import static com.micewine.emu.fragments.ShortcutsFragment.getBox64Preset;
import static com.micewine.emu.fragments.ShortcutsFragment.getBox64Version;
import static com.micewine.emu.fragments.ShortcutsFragment.getCpuAffinity;
import static com.micewine.emu.fragments.ShortcutsFragment.getD3DXRenderer;
import static com.micewine.emu.fragments.ShortcutsFragment.getDXVKVersion;
import static com.micewine.emu.fragments.ShortcutsFragment.getDisplaySettings;
import static com.micewine.emu.fragments.ShortcutsFragment.putDisplaySettings;
import static com.micewine.emu.fragments.ShortcutsFragment.getEnableDInput;
import static com.micewine.emu.fragments.ShortcutsFragment.getEnableXInput;
import static com.micewine.emu.fragments.ShortcutsFragment.getExeArguments;
import static com.micewine.emu.fragments.ShortcutsFragment.getExePath;
import static com.micewine.emu.fragments.ShortcutsFragment.getSelectedVirtualControllerPreset;
import static com.micewine.emu.fragments.ShortcutsFragment.getVKD3DVersion;
import static com.micewine.emu.fragments.ShortcutsFragment.getVulkanDriver;
import static com.micewine.emu.fragments.ShortcutsFragment.getVulkanDriverType;
import static com.micewine.emu.fragments.ShortcutsFragment.getWineD3DVersion;
import static com.micewine.emu.fragments.ShortcutsFragment.getWineESync;
import static com.micewine.emu.fragments.ShortcutsFragment.getWineServices;
import static com.micewine.emu.fragments.ShortcutsFragment.getWineVirtualDesktop;
import static com.micewine.emu.fragments.ShortcutsFragment.updateShortcuts;
import static com.micewine.emu.fragments.SoundSettingsFragment.generatePAFile;
import static com.micewine.emu.fragments.WinePrefixManagerFragment.createWinePrefix;
import static com.micewine.emu.fragments.WinePrefixManagerFragment.getSelectedWinePrefix;
import static com.micewine.emu.fragments.WinePrefixManagerFragment.getWinePrefixFile;
import static com.micewine.emu.fragments.WinePrefixManagerFragment.getWinePrefixes;
import static com.micewine.emu.utils.DriveUtils.parseUnixPath;
import static com.micewine.emu.utils.FileUtils.copyRecursively;
import static com.micewine.emu.utils.FileUtils.getFileExtension;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.input.InputManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.DisplayMetrics;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.micewine.emu.BuildConfig;
import com.micewine.emu.R;
import com.micewine.emu.adapters.AdapterBottomNavigation;
import com.micewine.emu.controller.ControllerUtils;
import android.widget.ProgressBar;
import com.micewine.emu.core.RatPackageManager;
import com.micewine.emu.fragments.RatDownloaderFragment;
import com.micewine.emu.fragments.CoreComponentsDownloaderFragment;
import com.micewine.emu.core.WineWrapper;
import com.micewine.emu.databinding.ActivityMainBinding;
import com.micewine.emu.fragments.AskInstallPackageFragment;
import com.micewine.emu.fragments.Box64PresetManagerFragment;
import com.micewine.emu.fragments.ControllerPresetManagerFragment;
import com.micewine.emu.fragments.EditGamePreferencesFragment;
import com.micewine.emu.fragments.FloatingFileManagerFragment;
import com.micewine.emu.fragments.SetupFragment;
import com.micewine.emu.fragments.ShortcutsFragment;
import com.micewine.emu.fragments.VirtualControllerPresetManagerFragment;
import com.micewine.emu.views.VirtualKeyboardInputView;
import com.micewine.emu.utils.FilePathResolver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import mslinks.ShellLink;

public class MainActivity extends AppCompatActivity {
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == null) return;

            switch (intent.getAction()) {
                case ACTION_RUN_WINE -> {
                    String exePath = intent.getStringExtra("exePath");
                    String exeArguments = intent.getStringExtra("exeArguments");
                    String driverName = intent.getStringExtra("driverName");
                    int driverType = intent.getIntExtra("driverType", MESA_DRIVER);
                    String box64Version = intent.getStringExtra("box64Version");
                    String box64Preset = intent.getStringExtra("box64Preset");
                    String displayResolution = intent.getStringExtra("displayResolution");
                    String d3dxRenderer = intent.getStringExtra("d3dxRenderer");
                    String wineD3D = intent.getStringExtra("wineD3D");
                    String dxvk = intent.getStringExtra("dxvk");
                    String vkd3d = intent.getStringExtra("vkd3d");
                    boolean esync = intent.getBooleanExtra("esync", true);
                    boolean services = intent.getBooleanExtra("services", false);
                    boolean virtualDesktop = intent.getBooleanExtra("virtualDesktop", false);
                    boolean enableXInput = intent.getBooleanExtra("enableXInput", true);
                    boolean enableDInput = intent.getBooleanExtra("enableDInput", true);
                    String cpuAffinity = intent.getStringExtra("cpuAffinity");

                    if (exeArguments == null) exeArguments = "";
                    if (driverName == null) driverName = "Global";
                    if (box64Version == null) box64Version = "Global";
                    if (box64Preset == null) box64Preset = "default";
                    if (displayResolution == null) displayResolution = "1280x720";
                    if (d3dxRenderer == null) d3dxRenderer = "DXVK";
                    if (wineD3D == null) wineD3D = listRatPackages(WINED3D).get(0).getFolderName();
                    if (dxvk == null) dxvk = listRatPackages(DXVK).get(0).getFolderName();
                    if (vkd3d == null) vkd3d = listRatPackages(VKD3D).get(0).getFolderName();
                    if (cpuAffinity == null) cpuAffinity = String.join(",", availableCPUs);

                    tmpDir.mkdirs();

                    if (driverName.equals("Global")) {
                        driverName = preferences.getString(SELECTED_VULKAN_DRIVER, "");
                        driverType = getVulkanDriverType(driverName);
                    }

                    String driverLibPath;
                    String adrenoToolsDriverPath = null;

                    RatPackageManager.RatPackage driverPackage = getPackageById(driverName);

                    if (driverPackage == null) return;

                    if (driverType == MESA_DRIVER) {
                        driverLibPath = driverPackage.getDriverLib();
                    } else if (driverType == ADRENO_TOOLS_DRIVER) {
                        List<RatPackageManager.RatPackage> adrenoToolsProviders = listRatPackages("AdrenoTools");

                        if (adrenoToolsProviders.isEmpty()) {
                            Toast.makeText(MainActivity.this, "AdrenoTools Provider Not Found", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        driverLibPath = adrenoToolsProviders.get(0).getDriverLib();
                        adrenoToolsDriverPath = driverPackage.getDriverLib();
                    } else {
                        driverLibPath = "";
                    }

                    generateICDFile(driverLibPath);
                    generateMangoHUDConfFile();
                    generatePAFile();

                    setSharedVars(
                            MainActivity.this,
                            box64Version,
                            box64Preset,
                            d3dxRenderer,
                            wineD3D,
                            dxvk,
                            vkd3d,
                            displayResolution,
                            esync,
                            services,
                            virtualDesktop,
                            enableXInput,
                            enableDInput,
                            cpuAffinity,
                            adrenoToolsDriverPath
                    );

                    String finalExeArguments = exeArguments;
                    new Thread(() -> runWine(exePath, finalExeArguments)).start();
                }
                case ACTION_SELECT_FILE_MANAGER -> {
                    String fileName = intent.getStringExtra("selectedFile");

                    if (fileName == null) return;
                    if (fileName.equals("..")) {
                        fileManagerCwd = new File(fileManagerCwd).getParent();
                        refreshFiles(MainActivity.this);
                        return;
                    }

                    File file = new File(fileName);
                    if (file.isFile()) {
                        String fileExtension = getFileExtension(file).toLowerCase();
                        switch (fileExtension) {
                            case "exe", "bat", "msi", "lnk" -> {
                                if (fileExtension.equals("lnk")) {
                                    try {
                                        ShellLink shellLink = new ShellLink(file);
                                        String parsedUnixPath = parseUnixPath(shellLink.resolveTarget());
                                        File targetFile = new File(parsedUnixPath);
                                        new EditGamePreferencesFragment(FILE_MANAGER_START_PREFERENCES, targetFile).show(getSupportFragmentManager(), "");
                                    } catch (Exception ignored) {
                                        Toast.makeText(MainActivity.this, R.string.lnk_read_fail, Toast.LENGTH_SHORT).show();
                                    }

                                    return;
                                }

                                new EditGamePreferencesFragment(FILE_MANAGER_START_PREFERENCES, file).show(getSupportFragmentManager(), "");
                            }
                            case "rat" -> {
                                ratCandidate = new RatPackageManager.RatPackage(file.getPath());

                                if (ratCandidate.getName() == null) return;

                                new AskInstallPackageFragment(RAT_PACKAGE).show(getSupportFragmentManager(), "");
                            }
                            case "zip" -> {
                                adToolsDriverCandidate = new RatPackageManager.AdrenoToolsPackage(file.getPath());

                                if (adToolsDriverCandidate.getName() == null) return;

                                new AskInstallPackageFragment(ADTOOLS_DRIVER_PACKAGE).show(getSupportFragmentManager(), "");
                            }
                            case "mwp" -> {
                                List<String> mwpLines;

                                try {
                                    mwpLines = Files.readAllLines(file.toPath());
                                } catch (IOException ignored) {
                                    mwpLines = null;
                                }

                                if (mwpLines != null && !mwpLines.isEmpty()) {
                                    switch (mwpLines.get(0)) {
                                        case "controllerPreset" -> mwpPresetCandidate = new AskInstallPackageFragment.MwpPreset(CONTROLLER_PRESET, file);
                                        case "virtualControllerPreset" -> mwpPresetCandidate = new AskInstallPackageFragment.MwpPreset(VIRTUAL_CONTROLLER_PRESET, file);
                                        case "box64Preset", "box64PresetV2" -> mwpPresetCandidate = new AskInstallPackageFragment.MwpPreset(BOX64_PRESET, file);
                                    }

                                    new AskInstallPackageFragment(MWP_PRESET_PACKAGE).show(getSupportFragmentManager(), "");
                                }
                            }
                        }
                    } else if (file.isDirectory()) {
                        fileManagerCwd = file.getPath();
                        refreshFiles(MainActivity.this);
                    }
                }
                case ACTION_INSTALL_RAT -> {
                    if (!(ratCandidate.getArchitecture().equals(deviceArch) || ratCandidate.getArchitecture().equals("any")) && !ratCandidate.getCategory().equals("Wine")) {
                        Toast.makeText(context, R.string.invalid_architecture_rat_file, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!installablePackagesCategories.contains(ratCandidate.getCategory())) {
                        Toast.makeText(context, R.string.unknown_package_category, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (checkPackageInstalled(ratCandidate.getName(), ratCandidate.getCategory(), ratCandidate.getVersion())) {
                        Toast.makeText(context, R.string.package_already_installed, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    new Thread(() -> {
                        SetupFragment setupFragment = new SetupFragment();

                        setupFragment.setupProgressCallback.setDialogText(getString(R.string.installing) + " " + ratCandidate.getName() + " (" + ratCandidate.getVersion() + ")...");
                        setupFragment.setupProgressCallback.setProgressBarIndeterminate(true);

                        setupFragment.show(getSupportFragmentManager(), "");

                        installRat(ratCandidate, setupFragment.setupProgressCallback);

                        setupFragment.dismiss();
                    }).start();
                }
                case ACTION_INSTALL_ADTOOLS_DRIVER -> {
                    boolean isPackageInstalled = checkPackageInstalled(adToolsDriverCandidate.getName() + " (AdrenoTools)", "AdrenoToolsDriver", adToolsDriverCandidate.getVersion());

                    if (isPackageInstalled) {
                        Toast.makeText(context, R.string.package_already_installed, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    new Thread(() -> {
                        SetupFragment setupFragment = new SetupFragment();

                        setupFragment.setupProgressCallback.setDialogText(getString(R.string.installing) + " " + adToolsDriverCandidate.getName() + " (" + adToolsDriverCandidate.getVersion() + ")...");
                        setupFragment.setupProgressCallback.setProgressBarIndeterminate(true);

                        setupFragment.show(getSupportFragmentManager(), "");

                        installADToolsDriver(adToolsDriverCandidate, setupFragment.setupProgressCallback);

                        setupFragment.dismiss();
                    }).start();
                }
                case ACTION_SELECT_ICON -> new FloatingFileManagerFragment(OPERATION_SELECT_ICON, wineDisksFolder.getPath()).show(getSupportFragmentManager(), "");
                case ACTION_SELECT_EXE_PATH -> new FloatingFileManagerFragment(OPERATION_SELECT_EXE, new File(getUnixPath(getExePath(selectedGameName))).getParent()).show(getSupportFragmentManager(), "");
                case ACTION_CREATE_WINE_PREFIX -> {
                    String winePrefix = intent.getStringExtra("winePrefix");
                    String wine = intent.getStringExtra("wine");

                    SetupFragment setupFragment = new SetupFragment();

                    setupFragment.setupProgressCallback.setDialogText(getString(R.string.creating_wine_prefix));
                    setupFragment.setupProgressCallback.setProgressBarIndeterminate(true);

                    setupFragment.show(getSupportFragmentManager(), "");

                    new Thread(() -> {
                        createWinePrefix(winePrefix, wine);
                        setSharedVars(MainActivity.this);

                        fileManagerCwd = fileManagerDefaultDir;
                        floatingFileManagerCwd = fileManagerDefaultDir;

                        setupFragment.dismiss();
                    }).start();
                }
            }
        }
    };
    private InputManager inputManager;
    private final InputManager.InputDeviceListener inputDeviceListener = new InputManager.InputDeviceListener() {
        @Override
        public void onInputDeviceAdded(int deviceId) {
            InputDevice.getDevice(deviceId);
        }

        @Override
        public void onInputDeviceChanged(int deviceId) {
            InputDevice device = InputDevice.getDevice(deviceId);
            if (device == null) return;

            if (connectedPhysicalControllers.stream().anyMatch(c -> c.id == deviceId)) return;
            if (((device.getSources() & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD)
                    || ((device.getSources() & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK)) {
                if (!device.getName().contains("uinput")) {
                    connectedPhysicalControllers.add(new ControllerUtils.PhysicalController(device.getName(), deviceId));
                    prepareControllersMappings();
                    sendBroadcast(new Intent(ACTION_UPDATE_CONTROLLERS_STATUS));
                }
            }
        }

        @Override
        public void onInputDeviceRemoved(int deviceId) {
            int index = -1;
            for (int i = 0; i < connectedPhysicalControllers.size(); i++) {
                if (connectedPhysicalControllers.get(i).id == deviceId) {
                    index = i;
                    break;
                }
            }

            if (index == -1) return;

            disconnectController(connectedPhysicalControllers.get(index).virtualControllerID);
            connectedPhysicalControllers.remove(index);
            sendBroadcast(new Intent(ACTION_UPDATE_CONTROLLERS_STATUS));
        }
    };

    private BottomNavigationView bottomNavigation;
    private ViewPager2 viewPager;
    private boolean cycloneMode = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        ControllerPresetManagerFragment.initialize(false);
        VirtualControllerPresetManagerFragment.initialize(false);
        Box64PresetManagerFragment.initialize();
        ShortcutsFragment.initialize();
        ControllerUtils.initialize(this);

        inputManager = (InputManager) getSystemService(INPUT_SERVICE);
        inputManager.registerInputDeviceListener(inputDeviceListener, null);

        cycloneMode = getIntent().getStringExtra("cycloneExeArgs") != null;

        boolean cycloneHeadless = cycloneMode;
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        if (!cycloneHeadless) {
            setContentView(binding.getRoot());
        } else {
            setContentView(R.layout.cyclone_setup);
        }

        setSharedVars(this);

        // On future here will have a code for check if app is updated and do specific data conversion if needed

        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(APP_VERSION, BuildConfig.VERSION_NAME);
        editor.apply();

        if (!cycloneHeadless) {
            bottomNavigation = findViewById(R.id.bottom_navigation);
            bottomNavigation.setOnItemSelectedListener(item -> {
                if (item.getItemId() == R.id.nav_shortcuts) {
                    selectedFragmentId = 0;
                    updateShortcuts();
                } else if (item.getItemId() == R.id.nav_settings) {
                    selectedFragmentId = 1;
                } else if (item.getItemId() == R.id.nav_file_manager) {
                    selectedFragmentId = 2;
                } else if (item.getItemId() == R.id.nav_about) {
                    selectedFragmentId = 3;
                }
                viewPager.setCurrentItem(selectedFragmentId);
                return true;
            });

            viewPager = findViewById(R.id.viewPager);
            viewPager.setAdapter(new AdapterBottomNavigation(this));
            viewPager.setUserInputEnabled(false);

            bottomNavigation.post(() -> bottomNavigation.setSelectedItemId(R.id.nav_shortcuts));
        }

        registerReceiver(receiver, new IntentFilter() {{
            addAction(ACTION_RUN_WINE);
            addAction(ACTION_INSTALL_RAT);
            addAction(ACTION_INSTALL_ADTOOLS_DRIVER);
            addAction(ACTION_SELECT_FILE_MANAGER);
            addAction(ACTION_SELECT_ICON);
            addAction(ACTION_SELECT_EXE_PATH);
            addAction(ACTION_CREATE_WINE_PREFIX);
        }});

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (getWinePrefixFile(winePrefix).exists()) {
                WineWrapper.clearDrives();

                List<StorageVolume> storageVolumes = ((StorageManager) getSystemService(Context.STORAGE_SERVICE)).getStorageVolumes();

                for (StorageVolume volume : storageVolumes) {
                    if (volume.isRemovable()) {
                        File volumeDirectory = volume.getDirectory();
                        if (volumeDirectory == null) return;

                        WineWrapper.addDrive(volume.getDirectory().getPath());
                    }
                }
            }
        }

        if (savedInstanceState == null) onNewIntent(getIntent());
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        boolean hasCore = haveAnyPackageByCategory(CORE);
        boolean hasWine = haveAnyPackageByCategory(WINE);
        boolean hasVulkanDriver = haveAnyPackageByCategory(VK_DRIVER);
        boolean hasDXVK = haveAnyPackageByCategory(DXVK);
        boolean hasVKD3D = haveAnyPackageByCategory(VKD3D);
        boolean hasWineD3D = haveAnyPackageByCategory(WINED3D);
        boolean hasBox64 = haveAnyPackageByCategory(BOX64);
        boolean hasWinePrefix = !(getWinePrefixes().isEmpty());
        boolean canProceed = (hasCore && hasWine && hasWinePrefix && hasVulkanDriver && hasDXVK && hasVKD3D && hasWineD3D && (deviceArch.equals("x86_64") || hasBox64));

        if (!canProceed && !cycloneMode) startActivity(new Intent(this, WelcomeActivity.class));
    }

    @Override
    protected void onResume() {
        super.onResume();

        runXServer();
        updateShortcuts();

        if (finishedWelcomeScreen) setupMiceWine();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        inputManager.unregisterInputDeviceListener(inputDeviceListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (selectedFragmentId == 2) {
                if (!fileManagerCwd.equals(fileManagerDefaultDir)) {
                    fileManagerCwd = new File(fileManagerCwd).getParent();
                    refreshFiles(this);
                    return true;
                }
            }
            if (selectedFragmentId > 0) {
                bottomNavigation.setSelectedItemId(R.id.nav_shortcuts);
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    private void installDXWrapper(String winePrefixName) {
        File winePrefix = getWinePrefixFile(winePrefixName);

        File driveC = new File(winePrefix, "drive_c");
        File system32 = new File(driveC, "windows/system32");
        File syswow64 = new File(driveC, "windows/syswow64");
        File selectedDXVKDir = new File(ratPackagesDir, selectedDXVK);
        File selectedVKD3DDir = new File(ratPackagesDir, selectedVKD3D);
        File selectedWineD3DDir = new File(ratPackagesDir, selectedWineD3D);

        switch (selectedD3DXRenderer) {
            case "DXVK" -> {
                if (selectedDXVKDir.exists()) {
                    File x64Folder = new File(selectedDXVKDir, "files/x64");
                    File x32Folder = new File(selectedDXVKDir, "files/x32");

                    if (x64Folder.exists() && x32Folder.exists()) {
                        copyRecursively(x64Folder, system32);
                        copyRecursively(x32Folder, syswow64);
                    }
                }
            }
            case "WineD3D" -> {
                if (selectedWineD3DDir.exists()) {
                    File x64Folder = new File(selectedWineD3DDir, "files/x64");
                    File x32Folder = new File(selectedWineD3DDir, "files/x32");

                    if (x64Folder.exists() && x32Folder.exists()) {
                        copyRecursively(x64Folder, system32);
                        copyRecursively(x32Folder, syswow64);
                    }
                }
            }
        }

        if (selectedVKD3DDir.exists()) {
            File x64Folder = new File(selectedVKD3DDir, "files/x64");
            File x32Folder = new File(selectedVKD3DDir, "files/x32");

            if (x64Folder.exists() && x32Folder.exists()) {
                copyRecursively(x64Folder, system32);
                copyRecursively(x32Folder, syswow64);
            }
        }
    }

    private void runWine(String exePath, String exeArguments) {
        if (exePath == null) exePath = "";
        if (exeArguments == null) exeArguments = "";
        installDXWrapper(winePrefix);

        boolean changedDpi = !(preferences.getBoolean(WINE_DPI_APPLIED, WINE_DPI_APPLIED_DEFAULT_VALUE));
        if (changedDpi) {
            int newDpi = preferences.getInt(WINE_DPI, WINE_DPI_DEFAULT_VALUE);
            WineWrapper.wine("reg add HKCU\\\\Control\\\\ Panel\\\\Desktop /t REG_DWORD /v LogPixels /d " + newDpi + " /f");

            SharedPreferences.Editor editor = preferences.edit();

            editor.putBoolean(WINE_DPI_APPLIED, true);
            editor.apply();
        }

        WineWrapper.killAll();

        File skCodec = new File("/system/lib64/libskcodec.so");
        if (skCodec.exists()) {
            runCommand(getEnv() + "LD_PRELOAD=" + skCodec + " " + usrDir + "/bin/pulseaudio --start --exit-idle=-1", true);
        }

        if (!wineServices) {
            new Thread(() -> {
                WineWrapper.waitForProcess("window_handler.exe");
                runCommand("pkill -9 services.exe", false);
            }).start();
        }

        if (preferences != null && !preferences.getBoolean("cycloneWineRegApplied", false)) {
            WineWrapper.wine("reg add HKCU\\\\Software\\\\Wine\\\\X11\\ Driver /t REG_SZ /v Managed /d N /f");
            WineWrapper.wine("reg add HKCU\\\\Software\\\\Wine\\\\X11\\ Driver /t REG_SZ /v Decorated /d N /f");
            WineWrapper.wine("reg add HKCU\\\\Control\\ Panel\\\\Desktop\\\\WindowMetrics /t REG_SZ /v CaptionHeight /d -15 /f");
            runCommand(getEnv() + "WINEPREFIX='" + winePrefixesDir + "/" + winePrefix + "' wineserver -k", false);
            preferences.edit().putBoolean("cycloneWineRegApplied", true).apply();
        }

        if (exePath.isEmpty()) {
            WineWrapper.wine("explorer /desktop=shell," + selectedResolution + " window_handler.exe " + getCpuHexMask(selectedCpuAffinity) + " TFM");
        } else {
            if (enableWineVirtualDesktop) {
                WineWrapper.wine("explorer /desktop=shell," + selectedResolution + " window_handler.exe " + getCpuHexMask(selectedCpuAffinity) + " '" + getSanitizedPath(exePath) + "' " + exeArguments, "'" + getSanitizedPath(Objects.requireNonNull(new File(exePath).getParent())) + "'");
            } else {
                new Thread(() -> WineWrapper.wine("start /unix C:\\\\windows\\\\window_handler.exe " + getCpuHexMask(selectedCpuAffinity))).start();

                WineWrapper.wine("'" + getSanitizedPath(exePath) + "' " + exeArguments, "'" + getSanitizedPath(new File(exePath).getParent()) + "'");
            }
        }

        WineWrapper.killAll();

        runOnUiThread(() -> Toast.makeText(this, getString(R.string.wine_is_closed), Toast.LENGTH_SHORT).show());
    }

    private boolean runningXServer = false;

    private void runXServer() {
        if (!haveAnyPackageByCategory(CORE)) return;
        if (runningXServer) return;

        tmpDir.mkdirs();

        runningXServer = true;

        new Thread(() -> {
            runCommand(
                    "env CLASSPATH=" + getClassPath() + " /system/bin/app_process / com.micewine.emu.CmdEntryPoint :0 &> /dev/null", true
            );
            runningXServer = false;
        }).start();
    }

    private String getClassPath() {
        return new File(getLibsPath()).getParentFile().getParentFile().getAbsolutePath() + "/base.apk";
    }

    private String getLibsPath() {
        return getApplicationInfo().nativeLibraryDir;
    }

    private void setupMiceWine() {
        appRootDir.mkdirs();
        ratPackagesDir.mkdirs();

        tmpDir.mkdirs();
        homeDir.mkdirs();
        iconsDir.mkdirs();
        winePrefixesDir.mkdirs();

        runCommand("chmod 700 -R " + appRootDir.getPath(), false);

        addGameToList(getString(R.string.desktop_mode_init), getString(R.string.desktop_mode_init), "");

        setSharedVars(this);

        List<RatPackageManager.RatPackage> winePackages = listRatPackages("Wine");

        if (winePackages.isEmpty()) return;

        String wine = winePackages.get(0).getFolderName();
        Intent createWinePrefixIntent = new Intent(ACTION_CREATE_WINE_PREFIX);

        createWinePrefixIntent.putExtra("wine", wine);
        createWinePrefixIntent.putExtra("winePrefix", winePrefix);

        sendBroadcast(createWinePrefixIntent);
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);

        String shortcutName = intent.getStringExtra("shortcutName");

        if (shortcutName != null) {
            String cycloneExeArgs = intent.getStringExtra("cycloneExeArgs");
            String cycloneSessionToken = intent.getStringExtra("cycloneSessionToken");

            if (cycloneExeArgs != null) {
                cycloneEnsureAndLaunch(shortcutName, cycloneExeArgs, cycloneSessionToken);
            } else {
                launchNamedGame(shortcutName, null);
            }
            return;
        }

        Uri uri = intent.getData();
        if (uri == null) return;

        String filePath = FilePathResolver.resolvePath(this, uri);
        if (filePath == null) return;

        new EditGamePreferencesFragment(FILE_MANAGER_START_PREFERENCES, new File(filePath)).show(getSupportFragmentManager(), "");
    }

    private boolean cycloneRuntimeReady() {
        boolean box64 = deviceArch.equals("x86_64") || haveAnyPackageByCategory(BOX64);
        return haveAnyPackageByCategory(CORE) && haveAnyPackageByCategory(WINE)
                && haveAnyPackageByCategory(VK_DRIVER) && haveAnyPackageByCategory(DXVK)
                && haveAnyPackageByCategory(VKD3D) && haveAnyPackageByCategory(WINED3D)
                && box64 && !getWinePrefixes().isEmpty();
    }

    public static volatile String cycloneLastExeArgs;
    public static volatile String cycloneLastSessionToken;

    private void cycloneEnsureAndLaunch(String shortcutName, String exeArgs, String sessionToken) {
        cycloneLastExeArgs = exeArgs;
        cycloneLastSessionToken = sessionToken;
        new Thread(() -> {
            boolean ready = cycloneRuntimeReady();
            android.util.Log.i("CycloneFlow", "start; token=" + (sessionToken == null ? "null" : sessionToken.length() + " chars") + " ready=" + ready);
            if (ready) {
                // the first-time hint only makes sense while provisioning
                runOnUiThread(() -> {
                    android.view.View hint = findViewById(R.id.cyclone_setup_hint);
                    if (hint != null) hint.setVisibility(android.view.View.GONE);
                });
            }
            if (!ready) {
                if (!cycloneProvision() || !cycloneRuntimeReady()) {
                    android.util.Log.e("CycloneFlow", "provision/ready failed");
                    cycloneFail("Setup failed. Check your connection and try again.");
                    return;
                }
            }
            android.util.Log.i("CycloneFlow", "runtime ready, winePrefix=" + winePrefix);
            setSharedVars(this);

            cycloneStartXServer();

            if (getExePath("Vortex").isEmpty()) {
                AdapterGame.GameItem item = new AdapterGame.GameItem("Vortex", "c:\\Vortex\\Vortex.exe", exeArgs, "");
                ShortcutsFragment.gameList.add(item);
                ShortcutsFragment.saveShortcuts();
            } else {
                putExeArguments("Vortex", exeArgs);
            }

            cycloneSelectVulkanDriver();

            android.content.SharedPreferences cyc = getSharedPreferences("cyclone", MODE_PRIVATE);
            boolean safeMode = cyc.getBoolean("safeMode", false);
            // "fast" trades some dynarec safety for speed; safe mode forces the stable set.
            boolean fast = !safeMode && "fast".equals(cyc.getString("perfPreset", "stable"));
            java.util.List<AdapterEnvVar.EnvVar> vortexEnv = new java.util.ArrayList<>();
            vortexEnv.add(new AdapterEnvVar.EnvVar("WINEDLLOVERRIDES", "windows.gaming.input="));
            vortexEnv.add(new AdapterEnvVar.EnvVar("BOX64_DYNAREC_BIGBLOCK", fast ? "1" : "0"));
            vortexEnv.add(new AdapterEnvVar.EnvVar("BOX64_DYNAREC_CALLRET", fast ? "1" : "0"));
            vortexEnv.add(new AdapterEnvVar.EnvVar("BOX64_DYNAREC_STRONGMEM", "1"));
            vortexEnv.add(new AdapterEnvVar.EnvVar("BOX64_DYNAREC_SAFEFLAGS", fast ? "1" : "2"));
            vortexEnv.add(new AdapterEnvVar.EnvVar("VORTEX_NO_UPDATE", "1"));
            vortexEnv.add(new AdapterEnvVar.EnvVar("WINEDEBUG", "warn+process"));
            vortexEnv.add(new AdapterEnvVar.EnvVar("RUST_BACKTRACE", "full"));
            int fpsCap = safeMode ? 60 : cyc.getInt("fpsCap", 60);
            if (fpsCap > 0)
                vortexEnv.add(new AdapterEnvVar.EnvVar("DXVK_FRAME_RATE", String.valueOf(fpsCap)));
            if (!safeMode && cyc.getBoolean("fpsHud", false))
                vortexEnv.add(new AdapterEnvVar.EnvVar("DXVK_HUD", "fps"));
            ShortcutsFragment.putEnvVars("Vortex", vortexEnv);

            if (preferences != null) {
                preferences.edit()
                        .putBoolean(RAM_COUNTER, false)
                        .putBoolean(ENABLE_DEBUG_INFO, false)
                        .putBoolean(ENABLE_MANGOHUD, false)
                        .putBoolean("displayStretch", true)
                        .apply();
            }

            String renderRes = !safeMode && cyc.getInt("renderHeight", 720) >= 1080 ? "1920x1080" : "1280x720";
            putDisplaySettings("Vortex", "16:9", renderRes);
            ShortcutsFragment.putWineVirtualDesktop("Vortex", false);

            // keep a layout the user rearranged in the overlay mapper; only rebuild the
            // default when it was never customized (or the preset is gone entirely)
            boolean controlsCustomized = cyc.getBoolean("controlsCustomized", false)
                    && VirtualControllerPresetManagerFragment.getVirtualControllerPreset("Cyclone") != null;
            if (!controlsCustomized) {
                String nativeRes = getNativeResolution(this);
                int nw = Integer.parseInt(nativeRes.split("x")[0]);
                int nh = Integer.parseInt(nativeRes.split("x")[1]);
                float big = nh * 0.20F;
                float small = nh * 0.14F;
                java.util.ArrayList<VirtualKeyboardInputView.VirtualButton> vButtons = new java.util.ArrayList<>();
                vButtons.add(new VirtualKeyboardInputView.VirtualButton(nw * 0.90F, nh * 0.80F, big, "Space", VirtualKeyboardInputView.SHAPE_CIRCLE));
                vButtons.add(new VirtualKeyboardInputView.VirtualButton(nw * 0.81F, nh * 0.55F, small, "LShift", VirtualKeyboardInputView.SHAPE_CIRCLE));
                vButtons.add(new VirtualKeyboardInputView.VirtualButton(nw * 0.81F, nh * 0.73F, small, "Chat", VirtualKeyboardInputView.SHAPE_CIRCLE));
                vButtons.add(new VirtualKeyboardInputView.VirtualButton(nw * 0.93F, nh * 0.13F, small, "Menu", VirtualKeyboardInputView.SHAPE_CIRCLE));
                // Optional buttons, off by default, toggled in the in-game settings panel.
                if (cyc.getBoolean("btnE", false))
                    vButtons.add(new VirtualKeyboardInputView.VirtualButton(nw * 0.90F, nh * 0.58F, small, "E", VirtualKeyboardInputView.SHAPE_CIRCLE));
                if (cyc.getBoolean("btnR", false))
                    vButtons.add(new VirtualKeyboardInputView.VirtualButton(nw * 0.73F, nh * 0.85F, small, "R", VirtualKeyboardInputView.SHAPE_CIRCLE));
                if (cyc.getBoolean("btnNumbers", false)) {
                    vButtons.add(new VirtualKeyboardInputView.VirtualButton(nw * 0.06F, nh * 0.42F, small, "1", VirtualKeyboardInputView.SHAPE_CIRCLE));
                    vButtons.add(new VirtualKeyboardInputView.VirtualButton(nw * 0.13F, nh * 0.42F, small, "2", VirtualKeyboardInputView.SHAPE_CIRCLE));
                    vButtons.add(new VirtualKeyboardInputView.VirtualButton(nw * 0.20F, nh * 0.42F, small, "3", VirtualKeyboardInputView.SHAPE_CIRCLE));
                }
                java.util.ArrayList<VirtualKeyboardInputView.VirtualAnalog> vAnalogs = new java.util.ArrayList<>();
                vAnalogs.add(new VirtualKeyboardInputView.VirtualAnalog(
                        nw * 0.12F, nh * 0.72F, nh * 0.34F, "W", "S", "A", "D", 0.35F));
                VirtualControllerPresetManagerFragment.putOrCreateVirtualControllerPreset(
                        "Cyclone", nativeRes, vButtons, vAnalogs, new java.util.ArrayList<>());
            }
            ShortcutsFragment.putSelectedVirtualControllerPreset("Vortex", "Cyclone");
            ShortcutsFragment.putVirtualControllerXInput("Vortex", false);

            cycloneEnsureLocalAppData();

            // dxvk only writes its state cache if the directory already exists
            //noinspection ResultOfMethodCallIgnored
            new File(homeDir, ".cache/dxvk-shader-cache").mkdirs();

            File exe = new File(winePrefixesDir, winePrefix + "/drive_c/Vortex/Vortex.exe");
            cycloneStatus(getString(R.string.cyclone_setup_game));
            cycloneInstallBundledExe(exe);
            android.util.Log.i("CycloneFlow", "vortex.exe path=" + exe.getPath() + " exists=" + exe.exists() + " len=" + (exe.exists() ? exe.length() : -1));
            if (!exe.exists() || exe.length() < 1024) {
                // fallback if the bundled copy somehow didn't install
                if (!downloadVortexExe(exe, sessionToken)) {
                    cycloneFail("Couldn't install Vortex.");
                    return;
                }
            }

            android.util.Log.i("CycloneFlow", "launching Vortex");
            cycloneStatus(getString(R.string.cyclone_setup_launching));
            String freshArgs = cycloneFreshPlayUri(exeArgs, sessionToken);
            final String launchArgs = freshArgs != null ? freshArgs : exeArgs;
            runOnUiThread(() -> launchNamedGame("Vortex", launchArgs));
        }).start();
    }

    private String cycloneFreshPlayUri(String exeArgs, String sessionToken) {
        if (exeArgs == null || sessionToken == null || sessionToken.isEmpty()) return null;
        java.util.regex.Matcher gm = java.util.regex.Pattern.compile("game=(\\d+)").matcher(exeArgs);
        if (!gm.find()) return null;
        String gameId = gm.group(1);
        try {
            okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
            okhttp3.Request req = new okhttp3.Request.Builder()
                    .url("https://playvortex.io/games/" + gameId + "/play")
                    .header("Cookie", "session_token=" + sessionToken)
                    .build();
            try (okhttp3.Response resp = client.newCall(req).execute()) {
                if (!resp.isSuccessful() || resp.body() == null) return null;
                String body = resp.body().string();
                java.util.regex.Matcher um = java.util.regex.Pattern.compile("vortex://[^\"'\\s\\\\<>]+").matcher(body);
                if (!um.find()) return null;
                String uri = um.group().replace("&amp;", "&");
                android.util.Log.i("CycloneFlow", "minted fresh play uri for game " + gameId);
                return "'" + uri + "'";
            }
        } catch (Exception e) {
            android.util.Log.e("CycloneFlow", "fresh play uri fetch failed: " + e);
            return null;
        }
    }

    private void cycloneStartXServer() {
        File xSocket = new File(tmpDir, ".X11-unix/X0");
        runCommand("rm -f " + tmpDir + "/.X11-unix/X0 " + tmpDir + "/.X0-lock", false);
        runCommand("pkill -9 -f CmdEntryPoint", false);
        runningXServer = false;
        try { Thread.sleep(300); } catch (InterruptedException ignored) {}
        runOnUiThread(this::runXServer);
        for (int i = 0; i < 150 && !xSocket.exists(); i++) {
            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
        }
        try { Thread.sleep(800); } catch (InterruptedException ignored) {}
        android.util.Log.i("CycloneFlow", "X server socket=" + xSocket.exists());
    }

    private boolean cycloneProvision() {
        try {
            setSharedVars(this);
            appRootDir.mkdirs();
            ratPackagesDir.mkdirs();
            tmpDir.mkdirs();
            homeDir.mkdirs();
            iconsDir.mkdirs();
            winePrefixesDir.mkdirs();

            cycloneStatus(getString(R.string.cyclone_setup_downloading));
            java.util.List<RatDownloaderFragment.RepoRatPackage> all = RatDownloaderFragment.fetchPackages();
            if (all.isEmpty()) return false;

            // fetchPackages() already filters to this device's arch (aarch64) + "any" + Wine,
            // so no arch excludes are needed here.
            java.util.List<RatDownloaderFragment.RepoRatPackage> sel = new java.util.ArrayList<>();
            addPkg(sel, all, "MiceWine-Core", null);
            addPkg(sel, all, "box64-0.4.0", null);
            addPkg(sel, all, "wine-10.10", null);
            addPkg(sel, all, "mesa-vulkan-freedreno-25.1.4", null);       // Turnip
            addPkg(sel, all, "mesa-vulkan-wrapper-25.1.4", "adrenotools"); // Wrapper
            addPkg(sel, all, "DXVK-1.9.4-any", null);
            addPkg(sel, all, "WineD3D-10.0-any", null);
            addPkg(sel, all, "VKD3D-2.8-any", null);

            // Every required component must resolve to a package.
            if (sel.size() < 8) return false;

            SetupFragment.ProgressCallback installCb = new SetupFragment.ProgressCallback() {
                public void onProgressChanged(int progress) { cycloneProgress(progress); }
                public void setProgressBarIndeterminate(boolean indeterminate) { }
                public void setDialogText(String text) { }
            };

            int total = sel.size();
            for (int i = 0; i < total; i++) {
                RatDownloaderFragment.RepoRatPackage p = sel.get(i);
                if (p == null) continue;
                final String label = p.ratPackage.name;
                final int n = i + 1;
                cycloneStatus("Downloading " + label + " (" + n + "/" + total + ")");
                CoreComponentsDownloaderFragment.downloadPackage(p.repoRatName,
                        (progress, mbps, read, len) -> cycloneProgress(progress));
                File f = new File(tmpDir, p.repoRatName);
                if (!f.exists()) return false;
                cycloneStatus("Installing " + label);
                RatPackageManager.installRat(new RatPackageManager.RatPackage(f.getPath()), installCb);
                f.delete();
            }

            cycloneStatus(getString(R.string.cyclone_setup_prefix));
            if (winePrefix == null) winePrefix = "default";
            setSharedVars(this);
            addGameToList(getString(R.string.desktop_mode_init), getString(R.string.desktop_mode_init), "");
            java.util.List<RatPackageManager.RatPackage> winePackages = listRatPackages("Wine");
            if (winePackages.isEmpty()) return false;
            cycloneStartXServer();
            createWinePrefix(winePrefix, winePackages.get(0).getFolderName());

            if (preferences != null) {
                preferences.edit().putBoolean("cycloneWineRegApplied", false).apply();
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** Add the first repo package whose filename contains {@code contains} and not {@code exclude}. */
    private void addPkg(java.util.List<RatDownloaderFragment.RepoRatPackage> out,
                        java.util.List<RatDownloaderFragment.RepoRatPackage> all,
                        String contains, String exclude) {
        for (RatDownloaderFragment.RepoRatPackage p : all) {
            String name = p.repoRatName;
            if (name.contains(contains) && (exclude == null || !name.contains(exclude))) {
                out.add(p);
                return;
            }
        }
    }

    private void cycloneStatus(String text) {
        runOnUiThread(() -> {
            android.widget.TextView tv = findViewById(R.id.cyclone_setup_status);
            if (tv != null) tv.setText(text);
        });
    }

    private void cycloneProgress(int progress) {
        runOnUiThread(() -> {
            ProgressBar pb = findViewById(R.id.cyclone_setup_progress);
            if (pb != null) {
                pb.setIndeterminate(false);
                pb.setProgress(progress);
            }
        });
    }

    private void cycloneFail(String msg) {
        runOnUiThread(() -> {
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            android.widget.TextView tv = findViewById(R.id.cyclone_setup_status);
            if (tv != null) tv.setText(msg);
        });
    }

    // Experimental "compat" build only: pin Vortex to the wrapper driver, which routes
    // Vulkan through the device's own system driver instead of Turnip. Turnip is Adreno-only,
    // so this is the only path that can produce an adapter on Mali GPUs (e.g. Galaxy A16).
    // The standard build leaves the driver alone and keeps Turnip.
    private void cycloneSelectVulkanDriver() {
        if (!com.micewine.emu.BuildConfig.USE_SYSTEM_VULKAN) return;
        try {
            for (String id : listRatPackagesId(com.micewine.emu.adapters.AdapterRatPackage.VK_DRIVER)) {
                RatPackageManager.RatPackage p = getPackageById(id);
                if (p != null && p.getDriverLib() != null
                        && p.getDriverLib().toLowerCase().contains("wrapper")) {
                    ShortcutsFragment.putVulkanDriver("Vortex", id);
                    android.util.Log.i("CycloneFlow", "compat: using system Vulkan driver " + id);
                    return;
                }
            }
            android.util.Log.e("CycloneFlow", "compat: wrapper Vulkan driver not found");
        } catch (Exception e) {
            android.util.Log.e("CycloneFlow", "compat: driver select failed: " + e);
        }
    }

    private void cycloneEnsureLocalAppData() {
        try {
            File userDir = new File(winePrefixesDir, winePrefix + "/drive_c/users/" + unixUsername);
            File appData = new File(userDir, "AppData");
            boolean symlink = java.nio.file.Files.isSymbolicLink(appData.toPath());
            if (symlink || !appData.isDirectory()) {
                if (symlink) //noinspection ResultOfMethodCallIgnored
                    appData.delete(); // removes the symlink, not its target
                String[] dirs = {
                        "Local/Temp", "Local/Vortex/logs",
                        "Local/Microsoft/Windows/History", "Local/Microsoft/Windows/INetCache",
                        "Local/Microsoft/Windows/INetCookies", "LocalLow",
                        "Roaming/Microsoft/Windows/Recent"
                };
                for (String d : dirs) //noinspection ResultOfMethodCallIgnored
                    new File(appData, d).mkdirs();
                android.util.Log.i("CycloneFlow", "AppData localized at " + appData);
            }
        } catch (Exception e) {
            android.util.Log.e("CycloneFlow", "AppData localize failed: " + e);
        }
    }

    // Vortex version encoded as major*1_000_000 + minor*1000 + patch, or -1 if the
    // binary carries no "Vortex vX.Y.Z" marker (the stale self-updating base has none).
    public static long readExeVersion(File f) {
        final byte[] marker = {'V', 'o', 'r', 't', 'e', 'x', ' ', 'v'};
        byte[] buf = new byte[1 << 16];
        int m = 0;
        boolean capturing = false;
        StringBuilder ver = new StringBuilder();
        try (java.io.InputStream in = new java.io.FileInputStream(f)) {
            int n;
            while ((n = in.read(buf)) != -1) {
                for (int i = 0; i < n; i++) {
                    int b = buf[i] & 0xFF;
                    if (capturing) {
                        if ((b >= '0' && b <= '9') || b == '.') {
                            ver.append((char) b);
                            if (ver.length() > 12) { capturing = false; ver.setLength(0); m = 0; }
                        } else {
                            long v = parseVer(ver.toString());
                            if (v >= 0) return v;
                            capturing = false; ver.setLength(0); m = 0;
                        }
                    } else if (b == (marker[m] & 0xFF)) {
                        m++;
                        if (m == marker.length) { capturing = true; ver.setLength(0); }
                    } else {
                        m = (b == (marker[0] & 0xFF)) ? 1 : 0;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return -1;
    }

    private static long parseVer(String s) {
        java.util.regex.Matcher mm = java.util.regex.Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)").matcher(s);
        if (mm.find())
            return Long.parseLong(mm.group(1)) * 1_000_000L + Long.parseLong(mm.group(2)) * 1000L + Long.parseLong(mm.group(3));
        return -1;
    }

    // Install `tmp` over `dest` only when it is a newer build, so nothing ever downgrades
    // the client. The old /download/windows base has no "Vortex vX.Y.Z" marker (-1), so a
    // real installed build always wins over it.
    private static boolean replaceIfNewer(File tmp, File dest) {
        if (tmp == null || !tmp.exists() || tmp.length() < 1024) {
            if (tmp != null) //noinspection ResultOfMethodCallIgnored
                tmp.delete();
            return dest.exists() && dest.length() > 1024;
        }
        if (!dest.exists()) {
            boolean ok = tmp.renameTo(dest);
            android.util.Log.i("CycloneFlow", "installed vortex.exe (fresh)");
            return ok && dest.exists();
        }
        long vTmp = readExeVersion(tmp), vDest = readExeVersion(dest);
        if (vTmp > vDest) {
            //noinspection ResultOfMethodCallIgnored
            dest.delete();
            boolean ok = tmp.renameTo(dest);
            android.util.Log.i("CycloneFlow", "vortex.exe upgraded v" + vDest + " -> v" + vTmp);
            return ok && dest.exists();
        }
        //noinspection ResultOfMethodCallIgnored
        tmp.delete();
        android.util.Log.i("CycloneFlow", "kept existing vortex.exe (v" + vDest + " >= v" + vTmp + ")");
        return true;
    }

    // Primary source: the Vortex.exe shipped inside the APK (assets/Vortex.exe).
    private void cycloneInstallBundledExe(File dest) {
        File tmp = new File(dest.getParentFile(), "Vortex.exe.bundle");
        try {
            //noinspection ResultOfMethodCallIgnored
            dest.getParentFile().mkdirs();
            //noinspection ResultOfMethodCallIgnored
            tmp.delete();
            try (java.io.InputStream in = getAssets().open("Vortex.exe");
                 java.io.OutputStream out = new java.io.FileOutputStream(tmp)) {
                byte[] buf = new byte[1 << 16];
                int r;
                while ((r = in.read(buf)) != -1) out.write(buf, 0, r);
            }
            replaceIfNewer(tmp, dest);
        } catch (Exception e) {
            android.util.Log.e("CycloneFlow", "bundled exe install failed: " + e);
            //noinspection ResultOfMethodCallIgnored
            tmp.delete();
        }
    }

    public static boolean downloadVortexExe(File dest, String sessionToken) {
        File tmp = new File(dest.getParentFile(), "Vortex.exe.dl");
        try {
            //noinspection ResultOfMethodCallIgnored
            dest.getParentFile().mkdirs();
            //noinspection ResultOfMethodCallIgnored
            tmp.delete();
            okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
            okhttp3.Request.Builder rb = new okhttp3.Request.Builder()
                    .url("https://playvortex.io/download/windows");
            if (sessionToken != null && !sessionToken.isEmpty()) {
                rb.header("Cookie", "session_token=" + sessionToken);
            }
            boolean extracted = false;
            try (okhttp3.Response resp = client.newCall(rb.build()).execute()) {
                android.util.Log.i("CycloneFlow", "vortex download HTTP " + resp.code()
                        + " hasToken=" + (sessionToken != null && !sessionToken.isEmpty()));
                if (!resp.isSuccessful() || resp.body() == null)
                    return dest.exists() && dest.length() > 1024;
                java.util.zip.ZipInputStream zis =
                        new java.util.zip.ZipInputStream(resp.body().byteStream());
                java.util.zip.ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    String n = entry.getName().toLowerCase();
                    if (!entry.isDirectory() && n.endsWith(".exe") && n.contains("vortex")) {
                        try (java.io.OutputStream out = new java.io.FileOutputStream(tmp)) {
                            byte[] buf = new byte[8192];
                            int r;
                            while ((r = zis.read(buf)) != -1) out.write(buf, 0, r);
                        }
                        zis.closeEntry();
                        extracted = tmp.exists() && tmp.length() > 1024;
                        break;
                    }
                    zis.closeEntry();
                }
            }
            if (!extracted) {
                //noinspection ResultOfMethodCallIgnored
                tmp.delete();
                android.util.Log.e("CycloneFlow", "no vortex .exe extracted");
                return dest.exists() && dest.length() > 1024;
            }
            return replaceIfNewer(tmp, dest);
        } catch (Exception e) {
            android.util.Log.e("CycloneFlow", "download exception: " + e);
            //noinspection ResultOfMethodCallIgnored
            tmp.delete();
            return dest.exists() && dest.length() > 1024;
        }
    }

    private void launchNamedGame(String name, String overrideArgs) {
        selectedGameName = name;

        Intent runActivityIntent = new Intent(this, EmulationActivity.class);
        Intent runWineIntent = new Intent(ACTION_RUN_WINE);

        String exePath = new File(getUnixPath(getExePath(name))).getPath();
        runWineIntent.putExtra("exePath", exePath);
        runWineIntent.putExtra("exeArguments", overrideArgs != null ? overrideArgs : getExeArguments(name));
        runWineIntent.putExtra("driverName", getVulkanDriver(selectedGameName));
        runWineIntent.putExtra("driverType", getVulkanDriverType(selectedGameName));
        runWineIntent.putExtra("box64Version", getBox64Version(selectedGameName));
        runWineIntent.putExtra("box64Preset", getBox64Preset(selectedGameName));
        runWineIntent.putExtra("displayResolution", getDisplaySettings(selectedGameName).get(1));
        runWineIntent.putExtra("virtualControllerPreset", getSelectedVirtualControllerPreset(selectedGameName));
        runWineIntent.putExtra("d3dxRenderer", getD3DXRenderer(selectedGameName));
        runWineIntent.putExtra("wineD3D", getWineD3DVersion(selectedGameName));
        runWineIntent.putExtra("dxvk", getDXVKVersion(selectedGameName));
        runWineIntent.putExtra("vkd3d", getVKD3DVersion(selectedGameName));
        runWineIntent.putExtra("esync", getWineESync(selectedGameName));
        runWineIntent.putExtra("services", getWineServices(selectedGameName));
        runWineIntent.putExtra("virtualDesktop", getWineVirtualDesktop(selectedGameName));
        runWineIntent.putExtra("enableXInput", getEnableXInput(selectedGameName));
        runWineIntent.putExtra("enableDInput", getEnableDInput(selectedGameName));
        runWineIntent.putExtra("cpuAffinity", getCpuAffinity(selectedGameName));

        sendBroadcast(runWineIntent);
        startActivity(runActivityIntent);
    }

    @SuppressLint("SdCardPath")
    public static final File appRootDir = new File("/data/data/" + com.micewine.emu.BuildConfig.APPLICATION_ID + "/files");
    public static File ratPackagesDir = new File(appRootDir + "/packages");
    public static String deviceArch = Build.SUPPORTED_ABIS[0].replace("arm64-v8a", "aarch64");
    public static final String unixUsername = runCommandWithOutput("whoami", false).replace("\n", "");
    public static File usrDir = new File(appRootDir + "/usr");
    public static File tmpDir = new File(usrDir + "/tmp");
    public static File homeDir = new File(appRootDir + "/home");
    public static File iconsDir = new File(homeDir, "/icons");
    public static boolean enableRamCounter = false;
    public static boolean enableCpuCounter = false;
    public static boolean enableDebugInfo = false;
    public static boolean enableDRI3 = false;
    public static boolean enableMangoHUD = false;
    public static String appLang = null;
    public static String box64LogLevel = null;
    public static Integer box64MMap32 = null;
    public static Integer box64Avx = null;
    public static Integer box64Sse42 = null;
    public static Integer box64DynarecBigBlock = null;
    public static Integer box64DynarecStrongMem = null;
    public static Integer box64DynarecWeakBarrier = null;
    public static Integer box64DynarecPause = null;
    public static Integer box64DynarecX87Double = null;
    public static Integer box64DynarecFastNan = null;
    public static Integer box64DynarecFastRound = null;
    public static Integer box64DynarecSafeFlags = null;
    public static Integer box64DynarecCallRet = null;
    public static Integer box64DynarecAlignedAtomics = null;
    public static Integer box64DynarecNativeFlags = null;
    public static Integer box64DynarecBleedingEdge = null;
    public static Integer box64DynarecWait = null;
    public static Integer box64DynarecDirty = null;
    public static Integer box64DynarecForward = null;
    public static Integer box64DynarecDF = null;
    public static Integer box64ShowSegv = null;
    public static Integer box64ShowBt = null;
    public static Integer box64NoSigSegv = null;
    public static Integer box64NoSigill = null;
    public static String wineLogLevel = null;
    public static String selectedBox64 = null;
    public static String selectedD3DXRenderer = null;
    public static String selectedWineD3D = null;
    public static String selectedDXVK = null;
    public static String selectedVKD3D = null;
    public static String selectedGLProfile = null;
    public static String selectedDXVKHud = null;
    public static String selectedMesaVkWsiPresentMode = null;
    public static String selectedTuDebugPreset = null;
    public static int selectedFragmentId = 0;
    public static String memoryStats = "??/??";
    public static String totalCpuUsage = "???%";
    public static File winePrefixesDir = new File(appRootDir + "/winePrefixes");
    public static File wineDisksFolder = null;
    public static String winePrefix = null;
    public static boolean wineESync = false;
    public static boolean wineServices = false;
    public static String selectedCpuAffinity = null;
    public static boolean enableWineVirtualDesktop = false;
    public static String selectedCore = null;
    public static String selectedWine = null;
    public static String fileManagerDefaultDir = "";
    public static String fileManagerCwd = null;
    public static String floatingFileManagerCwd = null;
    public static String selectedFilePath = "";
    public static String miceWineVersion = "MiceWine " + BuildConfig.VERSION_NAME + (BuildConfig.DEBUG ? " (git-" + BuildConfig.GIT_SHORT_SHA + ")" : "");
    public static String vulkanDriverDeviceName = null;
    public static String vulkanDriverDriverVersion = null;
    public static int screenFpsLimit = 60;
    public static int fpsLimit = 0;
    public static String paSink = null;
    public static String selectedResolution = null;
    public static boolean useAdrenoTools = false;
    public static boolean enableXInput = true;
    public static boolean enableDInput = true;
    public static File adrenoToolsDriverFile = null;
    public static SharedPreferences preferences = null;
    public static final Gson gson = new Gson();

    public static final String ACTION_RUN_WINE = "com.micewine.emu.ACTION_RUN_WINE";
    public static final String ACTION_INSTALL_RAT = "com.micewine.emu.ACTION_INSTALL_RAT";
    public static final String ACTION_INSTALL_ADTOOLS_DRIVER = "com.micewine.emu.ACTION_INSTALL_ADTOOLS_DRIVER";
    public static final String ACTION_SELECT_FILE_MANAGER = "com.micewine.emu.ACTION_SELECT_FILE_MANAGER";
    public static final String ACTION_SELECT_ICON = "com.micewine.emu.ACTION_SELECT_ICON";
    public static final String ACTION_SELECT_EXE_PATH = "com.micewine.emu.ACTION_SELECT_EXE_PATH";
    public static final String ACTION_CREATE_WINE_PREFIX = "com.micewine.emu.ACTION_CREATE_WINE_PREFIX";

    public static final String RAM_COUNTER = "ramCounter";
    public static final boolean RAM_COUNTER_DEFAULT_VALUE = true;

    public static final String CPU_COUNTER = "cpuCounter";
    public static final boolean CPU_COUNTER_DEFAULT_VALUE = false;

    public static final String ENABLE_DEBUG_INFO = "debugInfo";
    public static final boolean ENABLE_DEBUG_INFO_DEFAULT_VALUE = true;

    public static final String APP_VERSION = "appVersion";


    public static int strBoolToNum(boolean strBool) {
        return (strBool ? 1 : 0);
    }

    public static void setSharedVars(Activity activity, String adrenoToolsDriverPath) {
        setSharedVars(activity, null, null, null, null, null, null, null, null, null, null, null, null, null, adrenoToolsDriverPath);
    }

    public static void setSharedVars(Activity activity) {
        setSharedVars(activity, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    public static void setSharedVars(
            Activity activity,
            String box64Version,
            String box64Preset,
            String d3dxRenderer,
            String wineD3D,
            String dxvk,
            String vkd3d,
            String displayResolution,
            Boolean esync,
            Boolean services,
            Boolean virtualDesktop,
            Boolean enableXInputController,
            Boolean enableDInputController,
            String cpuAffinity,
            String adrenoToolsDriverPath
    ) {
        useAdrenoTools = (adrenoToolsDriverPath != null);
        adrenoToolsDriverFile = (adrenoToolsDriverPath != null ? new File(adrenoToolsDriverPath) : null);

        appLang = activity.getResources().getString(R.string.app_lang);

        selectedCore = preferences.getString(SELECTED_CORE, "");

        if (!selectedCore.isEmpty()) {
            runCommand("rm -rf " + usrDir, false);
            runCommand("ln -sf " + ratPackagesDir + "/" + selectedCore + "/files/usr " + usrDir, false);
            RatPackageManager.createSonameSymlinks(new File(ratPackagesDir, selectedCore + "/files/usr/lib"));
        }

        tmpDir.mkdirs();

        selectedBox64 = (box64Version != null ? box64Version : getBox64Version(selectedGameName));
        if ("Global".equals(selectedBox64)) selectedBox64 = preferences.getString(SELECTED_BOX64, "");

        box64LogLevel = preferences.getString(BOX64_LOG, String.valueOf(BOX64_LOG_DEFAULT_VALUE));

        box64ShowSegv = strBoolToNum(preferences.getBoolean(BOX64_SHOWSEGV, BOX64_SHOWSEGV_DEFAULT_VALUE));
        box64ShowBt = strBoolToNum(preferences.getBoolean(BOX64_SHOWBT, BOX64_SHOWBT_DEFAULT_VALUE));
        box64NoSigill = strBoolToNum(preferences.getBoolean(BOX64_NOSIGILL, BOX64_NOSIGILL_DEFAULT_VALUE));
        box64NoSigSegv = strBoolToNum(preferences.getBoolean(BOX64_NOSIGSEGV, BOX64_NOSIGSEGV_DEFAULT_VALUE));

        setBox64Preset(box64Preset);

        enableDRI3 = preferences.getBoolean(ENABLE_DRI3, ENABLE_DRI3_DEFAULT_VALUE);
        enableMangoHUD = preferences.getBoolean(ENABLE_MANGOHUD, ENABLE_MANGOHUD_DEFAULT_VALUE);
        wineLogLevel = preferences.getString(WINE_LOG_LEVEL, WINE_LOG_LEVEL_DEFAULT_VALUE);

        selectedD3DXRenderer = (d3dxRenderer != null ? d3dxRenderer : getD3DXRenderer(selectedGameName));
        selectedWineD3D = (wineD3D != null ? wineD3D : getWineD3DVersion(selectedGameName));
        selectedDXVK = (dxvk != null ? dxvk : getDXVKVersion(selectedGameName));
        selectedVKD3D = (vkd3d != null ? vkd3d : getVKD3DVersion(selectedGameName));

        selectedResolution = (displayResolution != null ? displayResolution : getDisplaySettings(selectedGameName).get(1));
        wineESync = (esync != null ? esync : getWineESync(selectedGameName));
        wineServices = (services != null ? services : getWineServices(selectedGameName));
        enableWineVirtualDesktop = (virtualDesktop != null ? virtualDesktop : getWineVirtualDesktop(selectedGameName));
        enableXInput = (enableXInputController != null ? enableXInputController : getEnableXInput(selectedGameName));
        enableDInput = (enableDInputController != null ? enableDInputController : getEnableDInput(selectedGameName));
        selectedCpuAffinity = (cpuAffinity != null ? cpuAffinity : getCpuAffinity(selectedGameName));

        selectedGLProfile = preferences.getString(SELECTED_GL_PROFILE, SELECTED_GL_PROFILE_DEFAULT_VALUE);
        selectedDXVKHud = preferences.getString(SELECTED_DXVK_HUD_PRESET, SELECTED_DXVK_HUD_PRESET_DEFAULT_VALUE);
        selectedMesaVkWsiPresentMode = preferences.getString(SELECTED_MESA_VK_WSI_PRESENT_MODE, SELECTED_MESA_VK_WSI_PRESENT_MODE_DEFAULT_VALUE);
        selectedTuDebugPreset = preferences.getString(SELECTED_TU_DEBUG_PRESET, SELECTED_TU_DEBUG_PRESET_DEFAULT_VALUE);

        enableRamCounter = preferences.getBoolean(RAM_COUNTER, RAM_COUNTER_DEFAULT_VALUE);
        enableCpuCounter = preferences.getBoolean(CPU_COUNTER, CPU_COUNTER_DEFAULT_VALUE);
        enableDebugInfo = preferences.getBoolean(ENABLE_DEBUG_INFO, ENABLE_DEBUG_INFO_DEFAULT_VALUE);

        screenFpsLimit = (int) ((WindowManager) activity.getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getRefreshRate();
        fpsLimit = preferences.getInt(FPS_LIMIT, screenFpsLimit);

        vulkanDriverDeviceName = getVulkanDriverInfo("deviceName") + (useAdrenoTools ? " (AdrenoTools)" : "");
        vulkanDriverDriverVersion = getVulkanDriverInfo("driverVersion").split(" ")[0];

        winePrefix = getSelectedWinePrefix();
        wineDisksFolder = new File(winePrefixesDir + "/" + winePrefix + "/dosdevices/");

        File winePrefixConfigFile = new File(winePrefixesDir + "/" + winePrefix + "/config");
        if (winePrefixConfigFile.exists()) {
            try {
                List<String> lines = Files.readAllLines(winePrefixConfigFile.toPath());
                selectedWine = lines.get(0);
            } catch (IOException ignored) {
            }

            fileManagerDefaultDir = wineDisksFolder.getPath();

            paSink = preferences.getString(PA_SINK, PA_SINK_DEFAULT_VALUE).toLowerCase();
        }
    }

    public static void setBox64Preset(String box64Preset) {
        String selectedBox64Preset = ((box64Preset != null && !box64Preset.equals("--")) ? box64Preset : preferences.getString(SELECTED_BOX64_PRESET, "default"));

        box64MMap32 = strBoolToNum(getBox64MMap32(selectedBox64Preset));
        box64Avx = getBox64Avx(selectedBox64Preset);
        box64Sse42 = strBoolToNum(getBox64Sse42(selectedBox64Preset));
        box64DynarecBigBlock = getBox64BigBlock(selectedBox64Preset);
        box64DynarecStrongMem = getBox64StrongMem(selectedBox64Preset);
        box64DynarecWeakBarrier = getBox64WeakBarrier(selectedBox64Preset);
        box64DynarecPause = getBox64Pause(selectedBox64Preset);
        box64DynarecX87Double = getBox64X87Double(selectedBox64Preset);
        box64DynarecFastNan = strBoolToNum(getBox64FastNan(selectedBox64Preset));
        box64DynarecFastRound = strBoolToNum(getBox64FastRound(selectedBox64Preset));
        box64DynarecSafeFlags = getBox64SafeFlags(selectedBox64Preset);
        box64DynarecCallRet = getBox64CallRet(selectedBox64Preset);
        box64DynarecAlignedAtomics = strBoolToNum(getBox64AlignedAtomics(selectedBox64Preset));
        box64DynarecNativeFlags = strBoolToNum(getBox64NativeFlags(selectedBox64Preset));
        box64DynarecWait = strBoolToNum(getBox64Wait(selectedBox64Preset));
        box64DynarecDirty = getBox64Dirty(selectedBox64Preset);
        box64DynarecForward = getBox64Forward(selectedBox64Preset);
        box64DynarecDF = strBoolToNum(getBox64DF(selectedBox64Preset));
    }

    public static void copyFile(InputStream input, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = input.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    private static String getVulkanDriverInfo(String info) {
        return runCommandWithOutput("echo $(" + getEnv() + " DISPLAY= vulkaninfo | grep " + info + " | cut -d '=' -f 2)", false);
    }

    public static void getMemoryInfo(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        long totalMemory;
        long availableMemory;
        long usedMemory;

        while (enableRamCounter) {
            activityManager.getMemoryInfo(memoryInfo);

            totalMemory = memoryInfo.totalMem / (1024 * 1024);
            availableMemory = memoryInfo.availMem / (1024 * 1024);
            usedMemory = totalMemory - availableMemory;

            memoryStats = usedMemory + "/" + totalMemory;

            try {
                Thread.sleep(800);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void getCpuInfo() {
        int availProcessors = Runtime.getRuntime().availableProcessors();

        while (enableCpuCounter) {
            String[] usageInfo = runCommandWithOutput("top -bqn 1 -o %CPU", false).split("\n");
            float usagePercentage = 0F;

            for (String usage : usageInfo) {
                usagePercentage += Float.parseFloat(usage.trim());
            }

            totalCpuUsage = usagePercentage / availProcessors + "%";

            try {
                Thread.sleep(800);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static String[] resolutions16_9 = new String[] {
            "640x360", "854x480",
            "960x540", "1280x720",
            "1366x768", "1600x900",
            "1920x1080", "2560x1440",
            "3840x2160", "7680x4320"
    };

    public static String[] resolutions18_9 = new String[] {
            "720x360", "960x480",
            "1080x540", "1440x720",
            "1536x768", "1800x900",
            "2160x1080", "2880x1440",
            "4320x2160", "8640x4320"
    };
    
    public static String[] resolutions4_3 = new String[] {
            "640x480", "800x600",
            "1024x768", "1280x960",
            "1400x1050", "1600x1200"
    };

    public static String getNativeResolution(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();

        windowManager.getDefaultDisplay().getRealMetrics(displayMetrics);

        if (displayMetrics.widthPixels > displayMetrics.heightPixels) {
            return displayMetrics.widthPixels + "x" + displayMetrics.heightPixels;
        } else {
            return displayMetrics.heightPixels + "x" + displayMetrics.widthPixels;
        }
    }

    public static String getPercentOfResolution(String originalResolution, int percent) {
        String[] resolution = originalResolution.split("x");
        int width = Integer.parseInt(resolution[0]) * percent / 100;
        int height = Integer.parseInt(resolution[1]) * percent / 100;

        return width + "x" + height;
    }

    public static List<String> getNativeResolutions(Activity activity) {
        ArrayList<String> parsedResolutions = new ArrayList<>();
        String nativeResolution = getNativeResolution(activity);

        parsedResolutions.add(nativeResolution);
        parsedResolutions.add(getPercentOfResolution(nativeResolution, 90));
        parsedResolutions.add(getPercentOfResolution(nativeResolution, 80));
        parsedResolutions.add(getPercentOfResolution(nativeResolution, 70));
        parsedResolutions.add(getPercentOfResolution(nativeResolution, 60));
        parsedResolutions.add(getPercentOfResolution(nativeResolution, 50));
        parsedResolutions.add(getPercentOfResolution(nativeResolution, 40));
        parsedResolutions.add(getPercentOfResolution(nativeResolution, 30));

        return parsedResolutions;
    }
}
