package com.itic.mobile.util.bean.jt_t808;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 异步SocketHelper
 * @author Andrea Ji
 * @version v1.0.0
 * 变更历史:
 * 提交日期  姓名  主线版本  修改原因
 * ---------------------------------------------------------*
 * 2015-02-08  Andrea Ji v1.0.0  第一次提交
 */
public class SocketHelper {

    /**
     * 发送数据实体
     */
    class SendDataModel{
        public SendDataModel(byte[] bts, MySendListener sendEndListener,
                             ActiveSendData data) {
            this.bts = bts;
            this.data = data;
            this.sendEndListener = sendEndListener;
        }
        byte[] bts;
        ActiveSendData data;
        MySendListener sendEndListener;

        public void OnMyAction(Exception ex) {
            if (sendEndListener != null)
                sendEndListener.EndSend(data, ex);
        }
    }

    /**
     * 重连数据监听接口
     */
    public interface ReConnIn {
        void ReConned();
    }

    /**
     * 发送数据监听接口
     */
    public interface MySendListener extends java.util.EventListener {
        void EndSend(ActiveSendData sendData, Exception ex);
    }

    /**
     * 接收数据监听接口
     */
    public interface MyReceiveListener extends java.util.EventListener {
        void EndReceive(byte[] bts, Exception ex);
    }

    /**
     * 发送线程
     */
    class SendThread extends AsyncTask<Integer,Void,Void> {

        @Override
        protected Void doInBackground(Integer... params) {
            int nowId = params[0];
            while (RunId == nowId) {
                if (ConnedState > 1 && sBuff.size() > 0) {
                    SendDataModel send = sBuff.get(0);
                    try {
                        outputStream.write(send.bts);
                        sBuff.remove(send);
                        send.OnMyAction(null);
                        continue;
                    } catch (IOException e) {

                        e.printStackTrace();
                        send.OnMyAction(e);
                    }
                }
                mySleep(1000);
            }
            return null;
        }
    }

    /**
     * 接受数据线程
     */
    class RecThread extends AsyncTask<Integer, Void, Void> {

        @Override
        protected Void doInBackground(Integer... params) {
            int nowId = params[0];
            int dlen = 0;
            byte[] tmp = new byte[50000];
            while (RunId == nowId) {
                if (ConnedState > 1) {
                    try {
                        dlen = inputStream.read(tmp);

                        if (dlen == -1) {
                            changeState(0);
                        } else if (dlen > 0) {

                            int i = 0;
                            boolean isend;// 当前为结束标记位
                            boolean flag7d;// 上一个字节是否为0x7d
                            MyBytesList lst;

                            /*
                             * 问题描述：上传数据频率过快时，接收数据时可能断在某包数据内，这样造成了被断数据包无法解析。
                             * 解决方法： 1.定义一个全局变量PGps记录下上次未解析数据；
                             * 2.解析完后判断出现7E的是否为结束符
                             * ，如果为结束符则将PGps赋值为NULL，反之记录下未解析部分数据
                             * 3.开始解析时判断首字节是否为7E，如果为7E则不处理断包问题，反之继续解析
                             */
                            if (PGps != null && ((dlen > 0 && tmp[0] != 0x7e)// 第一个字符不为7E
                                    || (dlen > 1 && tmp[0] == 0x7e && tmp[1] == 0x7e) // 首字母为7E接下来一个也为7E
                            ))// 处理接收断包问题
                            {
                                lst = PGps;
                                isend = false;
                                flag7d = PGps.get(PGps.size() - 1) == 0x7d;
                            } else {
                                lst = new MyBytesList();
                                isend = true;
                                flag7d = false;
                            }

                            while (i < dlen) {
                                if (tmp[i] == 0x7e)// 开始结束标记
                                {
                                    isend = !isend;
                                    if (isend)// 结束标记时解析数据
                                    {
                                        rBuff.add(lst);
                                    } else {
                                        // 清空暂存区
                                        lst.clear();
                                    }
                                } else if (flag7d)// 转义还原
                                {
                                    if (tmp[i] == 0x01)// 0x7d 0x01 => 0x7d
                                        lst.set(lst.size() - 1, (byte) 0x7d);
                                    else if (tmp[i] == 0x02)// 0x7d 0x02 => 0x7e
                                        lst.set(lst.size() - 1, (byte) 0x7e);
                                    else// 出错
                                    {
                                        StringBuilder sb = new StringBuilder();
                                        for (Byte byte1 : tmp) {
                                            sb.append(Integer
                                                    .toHexString(byte1));
                                        }
                                        Log.d("HVT300", sb.toString());
                                        break;
                                    }
                                } else
                                    lst.add(tmp[i]);
                                flag7d = tmp[i] == 0x7d;
                                i++;
                            }
                            PGps = isend ? null : lst;// 处理接收断包问题
                        }
                    } catch (IOException e) {

                        e.printStackTrace();
                    }
                } else {
                    mySleep(1000);
                }
            }
            return null;
        }
    }

    class MyBytesList extends ArrayList<Byte>{

        public byte[] toBytes() {
            Byte[] byteObjs = (Byte[]) cast(this,Byte.class);
            byte[] bytes = null;
            if(byteObjs.length > 0){
                for (int i = 0; i<byteObjs.length; i++){
                    bytes[i] = byteObjs[i].byteValue();
                }
                return bytes;
            }else{
                return null;
            }
        }
    }


    /**
     * 将数组array转换成clss代表的类型后返回
     * @param array
     * @param clss
     *
     * @return Object
     */
    private Object cast(Object array,Class clss){
        if(null==clss)
            throw new IllegalArgumentException("argument clss cannot be null");
        if(null==array)
            throw new IllegalArgumentException("argument array cannot be null");
        if(false==array.getClass().isArray())
            throw new IllegalArgumentException("argument array must be array");

        Object[] src=(Object[])array;
        Object[] dest=(Object[]) Array.newInstance(clss, src.length);
        System.arraycopy(src, 0, dest, 0, src.length);
        return dest;
    }

    /**
     * 检查线程
     */
    class checkThread extends AsyncTask<Integer, Void, Void> {

        @Override
        protected Void doInBackground(Integer... params) {
            int nowId = params[0];
            while (RunId == nowId) {
                if (ConnedState == 0) {
                    disConn();
                    conn();
                } else if (receiveListener != null) {
                    MyBytesList data = getData();
                    if (data != null)
                        receiveListener.EndReceive(data.toBytes(), null);
                }
                mySleep(1000);
            }
            return null;
        }
    }

    /**
     * 接收数据监听
     */
    private MyReceiveListener receiveListener;
    private Socket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private InetSocketAddress isa = null;
    /**
     * 服务器地址 支持域名
     */
    private String HOST;
    /**
     * 服务器端口
     */
    private int PORT ;
    /**
     * 连接超时时间
     */
    public int TIMEOUT = 5000;
    /**
     * 连接状态码
     * 0 未连接 <br/>
     * 1 已连接初始化中<br/>
     * 2 已连接初始化完成
     */
    private int ConnedState = 0;
    /**
     * 线程执行ID
     */
    private int RunId = 0;
    /**
     * 主动发送的数据
     */
    HashMap<Integer, ActiveSendData> AllSendData = new HashMap<Integer, ActiveSendData>();

    /**
     * 发送缓存数据
     */
    private ArrayList<SendDataModel> sBuff = new ArrayList<SendDataModel>();
    /**
     * 接受缓存数据
     */
    private ArrayList<MyBytesList> rBuff = new ArrayList<MyBytesList>();
    /**
     * 未解析数据
     */
    private MyBytesList PGps;
    /**
     * 重连监听
     */
    ReConnIn reConn;

    /**
     * 构造方法传入服务器地址和端口
     * @param HOST 服务器地址(支持域名)
     * @param PORT 服务器端口
     */
    public SocketHelper(String HOST, int PORT) {
        this.HOST = HOST;
        this.PORT = PORT;
    }

    /**
     * 获取连接状态
     * 0 未连接 <br/>
     * 1 已连接初始化中<br/>
     * 2 已连接初始化完成
     */
    public int getConnedState() {
        return ConnedState;
    }


    /**
     * 设置接收数据监听
     * @param receiveListener
     */
    public void setReceiveListener(MyReceiveListener receiveListener) {
        this.receiveListener = receiveListener;
    }

    /**
     * 启动连接
     */
    public void start() {
        isa = new InetSocketAddress(HOST, PORT);
        conn();
        new SendThread().execute(RunId);
        new RecThread().execute(RunId);
        new checkThread().execute(RunId);
    }

    /**
     * 关闭连接
     */
    public void stop() {
        RunId++;
        disConn();
    }

    /**
     * 连接服务器
     */
    void conn() {
        try {
            socket = null;
            socket = new Socket();
            socket.setReuseAddress(true);
            socket.connect(isa, TIMEOUT);
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            changeState(1);
            if (reConn != null)
                reConn.ReConned();
            changeState(2);
        } catch (IOException e) {
            e.printStackTrace();
            changeState(0);
        }
    }

    /**
     * 关闭连接
     */
    boolean disConn() {
        boolean flag = true;
        try {
            if (socket != null) {
                socket.shutdownInput();
                socket.shutdownOutput();
                try {
                    inputStream.close();
                    outputStream.close();
                } catch (IOException e) {

                }
                // 关闭socket
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            flag = false;
        }
        changeState(0);
        return flag;
    }

    /**
     * 修改状态码
     * @param state
     */
    void changeState(int state) {
        if (ConnedState != state) {
            ConnedState = state;
            // RunId++;
        }
    }

    /**
     * 开始异步发送数据
     * @param bts
     * @param sendEndListener
     * @param data
     */
    public void beginSend(byte[] bts, MySendListener sendEndListener,
                          ActiveSendData data) {
        sBuff.add(new SendDataModel(bts, sendEndListener, data));
    }

    /**
     * 同步发送数据
     * @param bts
     * @return boolean
     */
    public boolean SendData(byte[] bts) {
        if (ConnedState > 0) {
            try {
                outputStream.write(bts);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 获取第一包数据
     *
     * @return ArrayList<Byte>
     */
    public MyBytesList getData() {
        if (rBuff.size() > 0) {
            MyBytesList tmp = rBuff.get(0);
            rBuff.remove(0);
            return tmp;
        } else {
            return null;
        }
    }

    /**
     * 当前执行线程暂停一段时间
     *
     * @param time
     */
    public static void mySleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {

            e.printStackTrace();
        }
    }
}
