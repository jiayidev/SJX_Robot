package com.android.jdrd.robot.Test;

import java.util.LinkedList;
import java.util.List;

/**
 * 作者: jiayi.zhang
 * 时间: 2017/8/29
 * 描述: 发送命令
 */

public class Protocol {
    public static int throttle, yaw, pitch, roll;
    public static float pitchAng, rollAng, yawAng, voltage, alt, speedZ;
    // 发送的命令
    public static final int
            FLY_STATE = 16,//pitch、roll、yaw、Altitude、GPS_FIX?、Sat num、Voltage

            MN_PATTERN = 0,// 设置成磁导航模式
            MN_PATTERN_FUNCTION = 16,// 设置成脱轨运行模式
            UP_SPEED = 17,// 设置前进速度为500
            CLEAR_FAULT = 21;// 清除故障

    // 头以 *< 开始
    private static final String MSP_HEADER = "*<";

    // 获取命令数据
    public static byte[] getCommandData(int cmd) {

        List<Byte> cmdData = new LinkedList<Byte>();
        switch (cmd) {
            // 设置成磁导航模式
            case MN_PATTERN:
                cmdData.add((byte) ((0) & 0xff));
                cmdData.add((byte) ((18) & 0xff));
                break;
            // 设置成脱轨运行模式
            case MN_PATTERN_FUNCTION:
                cmdData.add((byte) ((1) & 0xff));
                cmdData.add((byte) ((19) & 0xff));
                break;
            // 设置前进速度为500
            case UP_SPEED:
                cmdData.add((byte) ((1) & 0xff));
                cmdData.add((byte) ((244) & 0xff));
                cmdData.add((byte) ((0) & 0xff));
                cmdData.add((byte) ((224) & 0xff));
                break;
            // 清除故障
            case CLEAR_FAULT:
                cmdData.add((byte) ((20) & 0xff));
                break;

        }

        // 获取数据的数量
        byte[] commandData = new byte[cmdData.size()];
        int i = 0;
        // 遍历数据
        for (byte b : cmdData) {
            commandData[i++] = b;
        }
        return commandData;
    }

    // 获取发送数据
    public static byte[] getSendData(int cmd, byte[] data) {
        if (cmd < 0)
            return null;
        // 存储数据
        List<Byte> bf = new LinkedList<Byte>();
        // 存储头数据  *<
        for (byte c : MSP_HEADER.getBytes()) {
            bf.add(c);
        }
        // 检索总数
        byte checksum = 0;
        // byte pl_size = (byte)((payload != null ? PApplet.parseInt(payload.length) : 0)&0xFF);
        byte dataSize = (byte) ((data != null) ? (data.length) : 0);
        bf.add(dataSize);
        checksum ^= (dataSize & 0xFF);

        bf.add((byte) (cmd & 0xFF));
        checksum ^= (cmd & 0xFF);

        if (data != null) {
            for (byte c : data) {
                bf.add((byte) (c & 0xFF));
                checksum ^= (c & 0xFF);
            }
        }
        // 检索的总长度
        bf.add(checksum);

        // 遍历所有数据
        byte[] sendData = new byte[bf.size()];
        int i = 0;
        for (byte b : bf) {
            sendData[i++] = b;
        }

        return (sendData);
    }


    static byte[] inBuf = new byte[256];
    static int p;

    public static int read32() {
        return (inBuf[p++] & 0xff) + ((inBuf[p++] & 0xff) << 8) + ((inBuf[p++] & 0xff) << 16) + ((inBuf[p++] & 0xff) << 24);
    }

    public static int read16() {
        return (inBuf[p++] & 0xff) + ((inBuf[p++]) << 8);
    }

    public static int read8() {
        return inBuf[p++] & 0xff;
    }

    public static final int
            IDLE = 0,
            HEADER_START = 1,
            HEADER_ARROW = 2,
            HEADER_SIZE = 3,
            HEADER_CMD = 4,
            HEADER_ERR = 5;

    public static boolean frameEnd = false;
    public static int c_state = IDLE;
    public static boolean err_rcvd;
    public static byte checksum = 0;
    public static byte cmd;
    public static int offset = 0, dataSize = 0;

    // 从初始数据帧中提取数据的过程
    static int pressDataIn(byte[] inData, int len) {
        int i = 0;
        int c;
        System.out.println("dataInLen:" + len + " " + inData[0]);

        for (i = 0; i < len; i++) {
            c = inData[i];
            if (c_state == IDLE) {//*
                c_state = (c == '*') ? HEADER_START : IDLE;
            } else if (c_state == HEADER_START) {
                if (c == '<') {    //right
                    c_state = HEADER_ARROW;
                } else if (c == '!') {
                    c_state = HEADER_ERR;    //错误的
                } else {
                    c_state = IDLE;
                }
            } else if (c_state == HEADER_ARROW || c_state == HEADER_ERR) {
                // is this an error message?
                err_rcvd = (c_state == HEADER_ERR);        // now we are expecting the payload size
                dataSize = (c & 0xFF);
                // reset index variables
                p = 0;
                offset = 0;
                checksum = 0;
                checksum ^= (c & 0xFF);
                // the command is to follow
                c_state = HEADER_SIZE;
            } else if (c_state == HEADER_SIZE) {
                cmd = (byte) (c & 0xFF);
                checksum ^= (c & 0xFF);
                c_state = HEADER_CMD;
            } else if (c_state == HEADER_CMD && offset < dataSize) {
                checksum ^= (c & 0xFF);
                inBuf[offset++] = (byte) (c & 0xFF);
            } else if (c_state == HEADER_CMD && offset >= dataSize) {
                frameEnd = true;
                // compare calculated and transferred checksum
                if ((checksum & 0xFF) == (c & 0xFF)) {//校验对比
                    if (err_rcvd) {
                        //System.err.println("Copter did not understand request type "+c);
                    } else {
                        // we got a valid response packet, evaluate it
                        evaluateCommand(cmd, dataSize);
                        System.out.println("cmd=" + cmd);
                    }
                } else //校验出错
                {
                    System.out.println("invalid checksum for command " + ((int) (cmd & 0xFF)) + ": " + (checksum & 0xFF) + " expected, got " + (int) (c & 0xFF));
                    System.out.print("<" + (cmd & 0xFF) + " " + (dataSize & 0xFF) + "> {");
                    for (i = 0; i < dataSize; i++) {
                        if (i != 0) {
                            System.err.print(' ');
                        }
                        System.out.print((inBuf[i] & 0xFF));
                    }
                    System.out.println("} [" + c + "]");
                    System.out.println(new String(inBuf, 0, dataSize));
                }
                c_state = IDLE;
                //  return -1;
            }
        }
        if (!frameEnd)
            return -2;
        else {
            frameEnd = false;
            if (err_rcvd)
                return -1;
            else
                return cmd;
        }
    }

    public static void evaluateCommand(byte cmd, int dataSize) {
        int i;
        int iCmd = (int) (cmd & 0xFF);
        switch (iCmd) {
            case FLY_STATE:
                rollAng = read16() / 10;
                pitchAng = read16() / 10;
                yawAng = read16() / 10;
                alt = read32() / 100.0f;        //cm
                voltage = read16() / 100.0f;
                speedZ = read16() / 1000.0f;

                System.out.println("pitch:" + pitchAng);
                break;
        }
    }

}
