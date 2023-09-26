package org.example.result;

import jdk.net.SocketFlow;


/**
 * @CLassname Result
 * @Description TODO
 * @Date 2021/5/27 11:13
 * @Created by lenovo
 */
public class Result<T> {
    private SocketFlow.Status status;
    private Data<T> data;

    public SocketFlow.Status getStatus() {
        return status;
    }

    public void setStatus(SocketFlow.Status status) {
        this.status = status;
    }

    public Data<T> getData() {
        return data;
    }

    public void setData(Data<T> data) {
        this.data = data;
    }
}
