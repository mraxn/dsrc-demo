package com.example.mraxn.obuapp;

import android.util.Log;

import java.nio.ByteBuffer;

import com.commsignia.v2x.client.ITSApplication;
import com.commsignia.v2x.client.exception.ClientException;
import com.commsignia.v2x.client.model.WsmpSendData;
import com.commsignia.v2x.client.model.dev.FacilityModule;
import com.commsignia.v2x.client.msgset.d.MessageSetD;

class ULClient
{
  private ITSApplication itsApplication = null;
  private String         hostAddr       = null;
  boolean         isConnected    = false;
  private int            wsmpPort       = 11;

  ULClient(String hostAddr)
  {
    this.hostAddr = hostAddr;
    itsApplication = new ITSApplication(5, hostAddr, 7942, new MessageSetD());

  }

  void Connect()
  {
    try
    {
      Log.i("UL", "Connecting to: " + hostAddr + ", port: " + 7942 + "...");

      itsApplication.connect();

      Log.i("UL", "Connected to: " + hostAddr + ", port: " + 7942 + "...");

    }
    catch (final Exception e)
    {
      isConnected = false;
      Log.e("UL", "Can't connect to: " + hostAddr + ", port: " + 7942 + "...");
      // ex.printStackTrace();
    }

    try
    {
      Log.i("UL", "Registering on port port: " + wsmpPort + "...");
      itsApplication.commands().registerBlocking();
      itsApplication.commands().setFacilityModuleStatus(FacilityModule.CAM, false);
      itsApplication.commands().wsmpBindBlocking(wsmpPort);
      isConnected = true;

      Log.i("UL", "WSMP: Registered connection on port " + wsmpPort + "...");
    }
    catch (final ClientException e)
    {
      isConnected = false;

      Log.e("UL", "Can't register / bind blocking!");
      // ex.printStackTrace();
      Close();
      Connect();
    }
  }

  void Send2Srv(String txJson)
  {
    final ByteBuffer alert2OBUAppData = ByteBuffer.wrap(txJson.getBytes());

    try
    {
      itsApplication.commands().wsmpSendBlocking(new WsmpSendData.Builder().withLLAddress("ff:ff:ff:ff:ff:ff")
          .withPortNumber(wsmpPort).withData(alert2OBUAppData.array()).withTxPowerDbm(20).build());
      Log.i("UL", "WSMP: Msg sent!");
    }
    catch (final ClientException e)
    {
      Log.e("UL", "WSMP: Can't sent!");
      Close();
      Connect();
      // e.printStackTrace();
    }

  }

  void Close()
  {
    try
    {
      Log.i("UL", "WSMP: Closing...");
      itsApplication.commands().wsmpCloseBlocking(wsmpPort);
      itsApplication.commands().deregisterBlocking();
      itsApplication.shutdown();
    }
    catch (final ClientException e)
    {
      Log.e("UL", "WSMP: Can't close /deregister!");
    }
  }

}
