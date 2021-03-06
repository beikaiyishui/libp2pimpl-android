package cn.vbyte.p2p;

import android.content.Context;
import android.os.Environment;
import android.os.Message;
import android.os.Handler;

import java.util.List;

import com.vbyte.update.*;

/**
 * Created by passion on 15-11-5.
 */
public final class VbyteP2PModule {
    private static final String DYNAMIC_LIB_NAME = "libp2pmodule";

    public static class Event {
        /**
         * 公共的，告诉应用已经初始化完毕
         */
        public static final int INITED = 10000000;

        public static final int EXIT = 10000001;
        /**
         * 公共的，告诉应用正在退出
         */
        public static final int EXITING = 10000002;                     //
        /**
         * 公共的，告诉应用已经完全退出
         */
        public static final int EXITED = 10000003;
        /**
         * 公共的，告诉应用流已经准备就绪
         */
        public static final int STREAM_READY = 10000004;                // 公共的，流已经准备就绪
        /**
         * 公共的，告诉应用配置已经就绪
         */
        public static final int CONF_READY = 10000005;                  // 公共的，配置已经就绪
        public static final int STUN_CONNECTED = 10000006;              // 公共的，点播，直播都在监听一个，且同一时刻，只能有一个监听此信号
        /**
         * 公共的，告诉应用已经加入P2P
         */
        public static final int JOINED = 10000007;                      // 公共的，属于tracker的
        /**
         * 公共的，告诉应用伙伴已经就绪
         */
        public static final int PARTNER_READY = 10000008;               // 公共的，伙伴已经就绪
        /**
         * 公共的，告诉应用有一个新伙伴加入你
         */
        public static final int NEW_PARTNER = 10000009;                 // 公共的，有一个新的伙伴
        /**
         * 公共的，表明P2P稳定
         */
        public static final int P2P_STABLE = 10000010;                  // 公共的，表明P2P稳定
        /**
         * 公共的，表明一次上报统计事件
         */
        public static final int REPORTED = 10000011;                    // 公共的，监听的上报信号
        
        /**
         * 公共的，表明卡播恢复
         */
        public static final int DATA_UNBLOCK = 10000012;                    // 公共的，监听的上报信号
    }

    public static class Error {
        /**
         * 表明配置服务不可用，此时加载失败
         */
        public static final int CONF_UNAVAILABLE = 10001000;               // 公共的，配置服务的
        /**
         * 表明认证失败，请检查传入的appId，appKey，appSecretKey是否正确，
         * 以及是否与当前应用对应得上。
         */
        public static final int AUTH_FAILED = 10001001;                    // 公共的，配置服务的
        /**
         * 表示conf的内容错误，不能被解析
         */
        public static final int CONF_INVALID = 10001002;                   // 公共的，配置服务的
        /**
         * 表明下载资源时网络太差，这很有可能是资源服务器的带宽不足引起的
         */
        public static final int SRC_UNSTABLE = 10001003;                   // 资源网络不好
        /**
         * 流媒体解析错误，请确认要播放的流是正确的能被解析的流媒体。
         */
        public static final int MEDIA_ERROR = 10001004;                    // 多媒体格式不对
        /**
         * 网络不好
         */
        public static final int BAD_NETWORK = 10001005;                    // 公共的
        /**
         * 这个说明穿透不成功，此错误通常因客户端所在网络限制造成的，将导致没有P2P加速。
         */
        public static final int STUN_FAILED = 10001006;                    // 公共的
        /**
         * 此错误说明某次获取伙伴失败了，不过不用太担心，它会一段时间后重试
         */
        public static final int JOIN_FAILED = 10001007;                    // 公共的，属于tracker的
        /**
         * 此错误事件表明一次上报事件失败，不会有其他影响
         */
        public static final int REPORT_FAILED = 10001008;                  // 公共的，上报服务的
        /**
         * 此错误表明收到未知类型的包，将忽略，一般很少发生
         */
        public static final int UNKNOWN_PACKET = 10001009;                 // 公共的，收发包逻辑
        /**
         * 此错误表明收到包数据签名不对，将忽略，一般很少发生
         */
        public static final int INVALID_PACKET = 10001010;                 // 公共的，收发包
        /**
         * 此错误表明数据不够，发生卡播
         */
        public static final int DATA_BLOCK = 10001011;                 // 公共的
    }

    // 持久保存SDK版本号
    private static String SDK_VERSION;
    private static VbyteP2PModule instance;

    /**
     * 新启动一个p2pLiveStream模块，注意四个参数绝对不能为null,在程序启动时调用
     * @param context 上下文
     * @param appId 应用唯一标识
     * @param appKey 应用密钥
     * @param appSecretKey 应用加密混淆字段，可选
     * @return P2PModule的唯一实例
     * @throws Exception 当参数为null或者p2p模块加载不成功时抛出异常
     */
    public static VbyteP2PModule create(Context context, String appId, String appKey, String appSecretKey)
            throws Exception {
        if (instance == null) {
            instance = new VbyteP2PModule(context, appId, appKey, appSecretKey);
        }
        return instance;
    }

    public static VbyteP2PModule getInstance() {
    	return instance;
    }

    /**
     * 获取native应用的版本号
     * @return P2PModuld SDK的版本号
     */
    public static String getVersion() {
        if (SDK_VERSION == null) {
            SDK_VERSION = VbyteP2PModule._version();
        }
        return SDK_VERSION;
    }

    /**
     * 启用调试模式，该模式会输出一些调试信息，对测试版本尤其有效
     */
    public static void enableDebug() {
    	VbyteP2PModule._enableDebug();
    }

    /**
     * 关闭调试模式。默认调试功能是关闭的，发布应用时也应该关闭调试模式。
     */
    public static void disableDebug() {
    	VbyteP2PModule._disableDebug();
    }

    /**
     * 获取P2PLiveStream模块的版本号
     * @return P2PLiveStream模块的版本号
     */
    private static native String _version();

    /**
     * 打开调试模式，默认是关闭调试模式的
     */
    private static native void _enableDebug();

    /**
     * 关闭调试模式，应用上线时应关闭调试模式
     */
    private static native void _disableDebug();

    /////////////////////////////////////////////////////////////
    /*=========================================================*/

    // 事件监听
    private Handler eventHandler = null;
    private Handler errorHandler = null;
    private Handler vbyteHandler = new VbyteHandler();
    private DynamicLibManager dynamicLibManager;
    // native代码对应的对象实例，标准做法
    private long _pointer;

    private VbyteP2PModule(Context context, String appId, String appKey, String appSecretKey)
            throws Exception {
        if (context == null || appId == null || appKey == null || appSecretKey == null) {
            throw new NullPointerException("context or appId or appKey or appSecretKey can't be null when init p2p live stream!");
        }

        System.loadLibrary("stun");
        System.loadLibrary("event");

        dynamicLibManager = new DynamicLibManager(context);
        String soFilePath = null;
        try {
            soFilePath = dynamicLibManager.locate(DYNAMIC_LIB_NAME);
        } catch (Exception e) {
            // 因获取不到程序版本号而导致的自动升级失败，默认使用安装时自带的
        }
        if(soFilePath == null) {
            System.loadLibrary("p2pmodule");
        } else {
            System.load(soFilePath);
        }
        dynamicLibManager.checkUpdate(DYNAMIC_LIB_NAME, getVersion());

        _pointer = this._construct();
        if (_pointer == 0) {
            throw new RuntimeException("Can not init P2P");
        }

        // TODO: 获取包名、cacheDir、diskDir等信息传入instance
        this._setContext(_pointer, context);
        String cacheDir = context.getCacheDir().getAbsolutePath();
        try {
            String diskDir = Environment.getExternalStorageDirectory().getAbsolutePath();
            this._setDiskDir(_pointer, diskDir);
        } catch (Exception e) {
            this._setDiskDir(_pointer, cacheDir);
        }
        this._setCacheDir(_pointer, cacheDir);
        this._setAppId(_pointer, appId);
        this._setAppKey(_pointer, appKey);
        this._setAppSecretKey(_pointer, appSecretKey);
    }

    /**
     * 设置EventHandler，注意该handler不能叠加，之前设置的handler将无效
     * @param handler 要设置的EventHandler实例
     */
    public void setEventHandler(Handler handler) {
        this.eventHandler = handler;
	}

    /**
     * 设置ErrorHandler，注意该handler不能叠加，之前设置的handler将无效
     * @param handler 要设置的ErrorHandler实例
     */
    public void setErrorHandler(Handler handler) {
        this.errorHandler = handler;
    }
    
    private void onEvent(int code, String msg) {
        List<BaseController.LoadEvent> loadQueue = BaseController.loadQueue;
        if (code == LiveController.Event.STOPPED || code == VodController.Event.STOPPED) {
            if (!loadQueue.isEmpty()) {
                loadQueue.remove(0);
            }
            if (!loadQueue.isEmpty()) {
                BaseController.LoadEvent loadEvent = loadQueue.get(0);
                if (loadEvent.videoType == BaseController.VIDEO_LIVE) {
                    LiveController.getInstance().loadDirectly(loadEvent.channel, loadEvent.resolution, loadEvent.startTime);
                } else {
                    VodController.getInstance().loadDirectly(loadEvent.channel, loadEvent.resolution, loadEvent.startTime);
                }
            }
        }
        Message message = Message.obtain();
        message.what = code;
        message.obj = msg;
        vbyteHandler.sendMessage(message);
        if (eventHandler != null) {
            eventHandler.sendMessage(Message.obtain(message));
        }
    }

    private void onError(int code, String msg) {
        Message message = Message.obtain();
        message.what = code;
        message.obj = msg;
        vbyteHandler.sendMessage(message);
        if (errorHandler != null) {
            errorHandler.sendMessage(Message.obtain(message));
        }
    }

    /**
     * native应用初始化
     * @return 成功返回native代码里面对应对象的指针，失败返回0
     */
    private native long _construct();
    
    /**
     * 设置context
     * @param context
     * @param pointer native层对应对象的指针
     */
    private native void _setContext(long pointer, Context context);

    /**
     * 设置可写缓存目录
     * @param cacheDir 应用缓存目录
     * @param pointer native层对应对象的指针
     */
    private native void _setCacheDir(long pointer, String cacheDir);

    /**
     * 设置永久磁盘目录
     * @param diskDir 应用无关的外存储器可写目录，
     *                如果设备没用外存储器，那将与cacheDir相同
     * @param pointer native层对应对象的指针
     */
    private native void _setDiskDir(long pointer, String diskDir);

    /**
     * 设置appId
     * @param appId 应用Id，为此应用唯一标识
     * @param pointer native层对应对象的指针
     */
    private native void _setAppId(long pointer, String appId);

    /**
     * 设置appKey
     * @param appKey 应用密钥
     * @param pointer native层对应对象的指针
     */
    private native void _setAppKey(long pointer, String appKey);

    /**
     * 设置appSecretKey
     * @param appSecretKey 应用混淆密钥
     * @param pointer native层对应对象的指针
     */
    private native void _setAppSecretKey(long pointer, String appSecretKey);
}
