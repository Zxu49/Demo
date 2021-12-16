# DemoDapp

This repo is sample dapp for Wallet Link SDK. 

WalletLink is an open protocol that lets users connect their mobile wallets to your DApp.

With the extend WalletLink SDK, your mobile dapp will be able to interact with mobile wallets and be able to sign web3 transactions and messages.

For more info check our extended [SDK](https://github.com/Zxu49/walletlink-mobile-sdk/tree/master/android)

The origin [repo](https://github.com/walletlink/walletlink-mobile-sdk)

### Dapp Side

Init the instance like wallet side
```
    val walletLink = WalletLink(notificationUrl, context)
```

1. Generated secret and session id into QR code, and established websocket to bridge server

```
    // This will connect to wallet link server using websocket
    // data is JSON RPC for establish host session, more info see our example format
    // session Id is 32 width randomly generated alphabet string 
    // secret is 64 width randomly generated alphabet string 

    walletLink.sendHostSessionRequest(data, sessionID, secret)
```

2. Using observable mode listing the response from connected socket

```
    walletLink.responseObservable
        .observeOn(serialScheduler)
        .subscribe { 
        // get socket response from socket
        }.addTo(disposeBag)
```
3. Using observable mode listing the address from connected socket

```
    walletLink.addressObservable
        .observeOn(serialScheduler)
        .subscribe {
        // get socket address from socket
        }
        .addTo(disposeBag)
```

4. Send personal string to bridge sever, then pass it to connected wallet  
```
    // data JsonRPCRequestPersonalDataDTO class encrypted by secret using AES
    val data = JSON.toJsonString(jsonRPC).encryptUsingAES256GCM(secret)
    walletLink.sendSignPersonalData(data, sessionID)
```

5. Send typed data (JSON-RPC) to bridge sever, then pass it to connected wallet  
```
    // data JsonRPCRequestTypedDataDTO class encrypted by secret using AES
    val data = JSON.toJsonString(jsonRPC).encryptUsingAES256GCM(secret)
    walletLink.sendSignTypedData(data, sessionID)
```

6. Submit the Transaction request to bridge sever, then pass it to connected wallet  

```
    // data JsonRPCRequestTransactionDataDTO class encrypted by secret using AES
    // the data will be wrapped inside the PublishEventDTO
    val data = JSON.toJsonString(jsonRPC).encryptUsingAES256GCM(secret)
    walletLink.sendStartTransaction(data,sessionID,secret)  
```

7. Cancel the previous the Transaction request to bridge sever, then pass it to connected wallet  

```
    // data JsonRPCRequestCancelDataDTO class encrpted by secret using AES
    // the data will be wrapped inside the PublishEventDTO
    val data = JSON.toJsonString(jsonRPC).encryptUsingAES256GCM(secret)
    walletLink.sendCancel(data,sessionID,secret)

```
