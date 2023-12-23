package CitadelWallet.im.status.keycard ;


/**
 * Utility methods to work with the Ed25519 curve.
 */


import javacard.security.*;
import com.nxp.id.jcopx.security.*;
import javacard.framework.Util;

/**
* ED25519 applet class
* 
*
* ------------------------------------------------------------------------------------------------------------------ *
*                                                                                                                    *
* Copyright (c) 2023 Secure Element Solutions, LLC. All Rights Reserved.   
* 
* DISCLAIMER OF WARRANTY/LIMITATION OF REMEDIES: unless otherwise agreed, Secure
* Element Solutions, LLC * has no obligation to support this software, and the software 
* is provided "AS IS", with no express or implied warranties of any kind, and
* Secure Element Solutions, LLC are not to be liable* for any damages, any relief, or for
* any claim by any third party, arising from use of this software. 
* This source code and any attachments are confidential and intended only for the individual or entity to whom are authorized
* by Secure Element Solutions, LLC.
* Any dissemination or use of this information by a person other than the intended recipient is unauthorized and is illegal                                                    *
*                                                                                                                    *                                                                                                                 *
* ------------------------------------------------------------------------------------------------------------------ *
*/
public class ed25519 {

	static final boolean DEBUG_MODE = false;
	
	private ECPublicKeyWithPredefinedParameters eccPubKey;
	private ECPrivateKeyWithPredefinedParameters eccPriKey;
	
	private final byte[] DebugMasterPrivateKey = {0x43, (byte)0x88, 0x73, 0x08, (byte)0xC6, 0x0C, (byte)0xFD, (byte)0xE7, (byte)0xE1, 0x2B, 0x13, (byte)0xAB, (byte)0xFE, (byte)0xEF, 0x3E, 0x76,
			0x0C, 0x11, 0x0C, (byte)0xA6, 0x7E, 0x45, (byte)0x9B, 0x3F, 0x01, 0x4D, 0x00, (byte)0x8F, (byte)0xE6, 0x08, (byte)0xD7, 0x6A};
//big Endian
//	private final byte[] DebugMasterPublicKey = { (byte)0xAA, (byte)0xc7, (byte)0xB8, (byte)0xc8, (byte)0xF4, (byte)0xB2, 0x32, 0x12, (byte)0x98, 0x05, (byte)0x8B, 0x19, 0x2e, 0x3d, 0x0a, (byte)0xD3,
//			0x61, (byte)0xc0, (byte)0xd9, (byte)0x94, 0x24, 0x30, (byte)0xef, (byte)0xb7, (byte)0x44, (byte)0xe4, (byte)0xbb, (byte)0x45, (byte)0x87, 0x73, 0x65, (byte)0xd0 };

// Little Endian
	private final byte[] DebugMasterPublicKey = { (byte)0xD3, 0x0A, 0x3D, 0x2E, 0x19, (byte)0x8B, 0x05, (byte)0x98, 0x12, 0x32, (byte)0xB2, (byte)0xF4, (byte)0xC8, (byte)0xB8, (byte)0xC7, (byte)0xAA,
			(byte)0xD0, 0x65, 0x73, (byte)0x87, 0x45, (byte)0xBB, (byte)0xE4, 0x44, (byte)0xB7, (byte)0xEF, 0x30, 0x24, (byte)0x94, (byte)0xD9, (byte)0xC0, 0x61 };

	public ed25519()
	{
		eccPriKey = (ECPrivateKeyWithPredefinedParameters)KeyBuilderX.buildKey(KeyBuilderX.ALG_TYPE_ED25519_PRIVATE, (byte)0x00 );
		eccPubKey = (ECPublicKeyWithPredefinedParameters)KeyBuilderX.buildKey(KeyBuilderX.ALG_TYPE_ED25519_PUBLIC, (byte)0x00 );

		if(DEBUG_MODE )
		{
			eccPriKey.setS(DebugMasterPrivateKey, (short)0, (short)DebugMasterPrivateKey.length);
			eccPubKey.setW(DebugMasterPublicKey, (short)0, (short)DebugMasterPublicKey.length);
		}
		else
			KeyBuilderX.genKeyPair(eccPriKey, eccPubKey);
			
	}
	
	
	public short SignData( byte [] DataToSign, short DataToSignOffset, short DataToSignLength, byte[] SignatureBuffer, short SignatureOffset)
	{
		short SigLen = CryptoBaseX.sign(eccPriKey, CryptoBaseX.ALG_ED25519PH_SHA_512, DataToSign, DataToSignOffset, DataToSignLength, SignatureBuffer, SignatureOffset);
		BigToLittleEndian(SignatureBuffer, (short)0,(short)32);
		BigToLittleEndian(SignatureBuffer, (short)32,(short)32);	
		return SigLen;
	}

	public short SignData( ECPrivateKeyWithPredefinedParameters privatekey, byte [] DataToSign, short DataToSignOffset, short DataToSignLength, byte[] SignatureBuffer, short SignatureOffset)
	{
		short SigLen = CryptoBaseX.sign(privatekey, CryptoBaseX.ALG_ED25519PH_SHA_512, DataToSign, DataToSignOffset, DataToSignLength, SignatureBuffer, SignatureOffset);
		BigToLittleEndian(SignatureBuffer, (short)0,(short)32);
		BigToLittleEndian(SignatureBuffer, (short)32,(short)32);	
		return SigLen;
	}
	
	public void RegenerateKeyPair ( )
	{
		KeyBuilderX.genKeyPair(eccPriKey, eccPubKey);		
	}

	public short GetPublicKey( byte [] OutData, short offset)
	{
		short length = eccPubKey.getW(OutData, offset);
		if( !DEBUG_MODE)
			BigToLittleEndian(OutData, (short)0,(short)length);
		return (short)(length);
	}

	public ECPrivateKeyWithPredefinedParameters GetPrivateKey()
	{
		return eccPriKey;
	}

	public short GetPrivateKey( byte [] OutData, short offset)
	{
		short length = eccPriKey.getS(OutData, offset);
		return (short)(length);
	}

	public ECPublicKeyWithPredefinedParameters GetPublicKey()
	{
		return eccPubKey;
	}
	
	public boolean VerifySignature( byte [] DataToVerify, short DataToVerifyOffset, short DataToVerifyLength, byte [] SignatureBuffer, short SignatureOffset, short SignatureLength )
	{
		boolean status = false;
		short verifystatus=0;
		try{
			verifystatus = CryptoBaseX.verify(eccPubKey, CryptoBaseX.ALG_ED25519PH_SHA_512, DataToVerify, DataToVerifyOffset, DataToVerifyLength, SignatureBuffer, SignatureOffset, SignatureLength);
		}
		catch(CryptoException ce){
			status = false;
		}
		if( verifystatus == ConstantX.TRUE16)
			status = true;
		return status;
	}

	public boolean VerifySignature( byte[] Incomingpublickey, short Keylength, byte [] DataToVerify, short DataToVerifyOffset, short DataToVerifyLength, byte [] SignatureBuffer, short SignatureOffset, short SignatureLength )
	{
		boolean status = false;
		short verifystatus=0;
		ECPublicKeyWithPredefinedParameters publickey = (ECPublicKeyWithPredefinedParameters)KeyBuilderX.buildKey(KeyBuilderX.ALG_TYPE_ED25519_PUBLIC, (byte)0x00 );
		LittleToBigEndian(Incomingpublickey, (short)0,(short)Keylength);
		publickey.setW(Incomingpublickey, (short)0, Keylength);
		
		try{
			verifystatus = CryptoBaseX.verify(publickey, CryptoBaseX.ALG_ED25519PH_SHA_512, DataToVerify, DataToVerifyOffset, DataToVerifyLength, SignatureBuffer, SignatureOffset, SignatureLength);
		}
		catch(CryptoException ce){
			status = false;
		}
		if( verifystatus == ConstantX.TRUE16)
			status = true;
		return status;
	}
	
	
	public boolean VerifySignature( ECPublicKeyWithPredefinedParameters publickey, byte [] DataToVerify, short DataToVerifyOffset, short DataToVerifyLength, byte [] SignatureBuffer, short SignatureOffset, short SignatureLength )
	{
		boolean status = false;
		short verifystatus=0;
		try{
			verifystatus = CryptoBaseX.verify(publickey, CryptoBaseX.ALG_ED25519PH_SHA_512, DataToVerify, DataToVerifyOffset, DataToVerifyLength, SignatureBuffer, SignatureOffset, SignatureLength);
		}
		catch(CryptoException ce){
			status = false;
		}
		if( verifystatus == ConstantX.TRUE16)
			status = true;
		return status;
	}
	
	private void BigToLittleEndian(byte [] Data, short Offset, short length )
	{
		byte[] TempBuffer = new byte[length];
		short t;
		for( short j = 0; j < length;j++ )
		{
			t=(short)(length - 1 -j + Offset);
			TempBuffer[j] = Data[t];
		}
		Util.arrayCopyNonAtomic(TempBuffer, (short)0, Data, (short)Offset, (short)length);
	}

	private void LittleToBigEndian(byte [] Data, short Offset, short length )
	{
		byte[] TempBuffer = new byte[length];
		short t;
		for( short j = 0; j < length;j++ )
		{
			t=(short)(length - 1 -j + Offset);
			TempBuffer[t] = Data[j];
		}
		Util.arrayCopyNonAtomic(TempBuffer, (short)0, Data, (short)Offset, (short)length);
	}

}

