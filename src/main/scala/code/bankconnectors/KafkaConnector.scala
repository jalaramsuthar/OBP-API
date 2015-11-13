package code.bankconnectors

/*
Open Bank Project - API
Copyright (C) 2011-2015, TESOBE / Music Pictures Ltd

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see http://www.gnu.org/licenses/.

Email: contact@tesobe.com
TESOBE / Music Pictures Ltd
Osloerstrasse 16/17
Berlin 13359, Germany
*/


import java.util.{Calendar, UUID, Date}

import code.model._
import net.liftweb.common.{Loggable, Empty, Full, Box, Failure}
import net.liftweb.mapper._
import net.liftweb.util.Helpers._
import net.liftweb.util.{False, Props}

import scala.concurrent.ops._
import scala.concurrent.duration._

import kafka.utils.{ZkUtils, ZKStringSerializer}
import org.I0Itec.zkclient.ZkClient
import java.util.Properties
import kafka.consumer.Consumer
import kafka.consumer._
import kafka.consumer.KafkaStream
import kafka.message._
import kafka.producer.{KeyedMessage, Producer, ProducerConfig}

import code.model.dataAccess.{MappedBank}

object KafkaConnector extends Connector with Loggable {

  import ZooKeeperUtils._

  val ZK_HOST: String = "localhost:2181"
  val TPC_RESPONSE: String = "Response"
  val TPC_REQUEST: String = "Request"

  //gets banks handled by this connector
  override def getBanks: List[Bank] = {
    val brokerList: String = getBrokers(ZK_HOST).mkString(",")
    val reqId: String = UUID.randomUUID().toString

    // Send request to kafka, mark it with reqId so we can fetch the corresponding answer
    val producer: KafkaProducer = new KafkaProducer(TPC_REQUEST, brokerList)
    producer.send(reqId, "getBanks:{}")

    // Request sent, now we wait for response with the same reqId
    val consumer = new KafkaConsumer(ZK_HOST, "1", TPC_RESPONSE, 0)
    val r = consumer.getResponse(reqId)
    val res: List[Bank] = List(
        MappedBank.create
        .permalink(r.getOrElse("permalink", ""))
        .fullBankName(r.getOrElse("fullBankName", ""))
        .shortBankName(r.getOrElse("shortBankName", ""))
        .logoURL(r.getOrElse("logoURL", ""))
        .websiteURL(r.getOrElse("websiteURL", ""))
    )
    res
  }

  //gets bank identified by id
  override def getBank(bankId: code.model.BankId): Box[Bank] = {
    val reqId: String = UUID.randomUUID().toString

    // Send request to kafka, mark it with reqId so we can fetch the corresponding answer
    val producer: KafkaProducer = new KafkaProducer(TPC_REQUEST, getBrokers(ZK_HOST).mkString(","))
    producer.send(reqId, "getBank:{bankId:" + bankId.toString + "}")

    // Request sent, now we wait for response with the same reqId
    val consumer = new KafkaConsumer(ZK_HOST, "1", TPC_RESPONSE, 0)
    val r = consumer.getResponse(reqId)
    Full( MappedBank.create
         .permalink(r.getOrElse("permalink", ""))
         .fullBankName(r.getOrElse("fullBankName", ""))
         .shortBankName(r.getOrElse("shortBankName", ""))
         .logoURL(r.getOrElse("logoURL", ""))
         .websiteURL(r.getOrElse("websiteURL", ""))
    )
  }

  def getTransactions(bankId: BankId,accountID: AccountId,queryParams: OBPQueryParam*): Box[List[Transaction]] = {
    val brokerList: String = getBrokers(ZK_HOST).mkString(",")
    val reqId: String = UUID.randomUUID().toString

    // Send request to kafka, mark it with reqId so we can fetch the corresponding answer
    val producer: KafkaProducer = new KafkaProducer(TPC_REQUEST, brokerList)
    producer.send(reqId, "getTransactions:{}")

    // Request sent, now we wait for response with the same reqId
    val consumer = new KafkaConsumer(ZK_HOST, "1", TPC_RESPONSE, 0)
    val r = consumer.getResponse(reqId)
    /*
    Full( MappedTransaction.create
          .bank(r.getOrElse("bank", ""))
          .account(r.getOrElse("account", ""))
          .transactionId(r.getOrElse("transactionId", ""))
          .transactionType(r.getOrElse("transactionType", ""))
          .amount(r.getOrElse("transactionType", "").toLong)
          .newAccountBalance(r.getOrElse("transactionType", "").toLong)
          .currency(r.getOrElse("currency", ""))
          .tStartDate(null) //r.getOrElse("tStartDate", ""))
          .tFinishDate(null) //r.getOrElse("tFinishDate", ""))
          .description(r.getOrElse("description", ""))
          //.counterpartyAccountHolder(null) //r.getOrElse("counterpartyAccountHolder", ""))
          //.counterpartyAccountNumber(List[Transaction](null)) //r.getOrElse("counterpartyAccountNumber", ""))
    )
    */
    Full(List(MappedTransaction.create, MappedTransaction.create))
  }

  def accountExists(bankId: code.model.BankId,accountNumber: String): Boolean = ???
  def addCashTransactionAndUpdateBalance(account: code.bankconnectors.KafkaConnector.AccountType,cashTransaction: code.tesobe.CashTransaction): Unit = ???
  def createBankAndAccount(bankName: String,bankNationalIdentifier: String,accountNumber: String,accountHolderName: String): (code.model.Bank, code.model.BankAccount) = ???
  def createImportedTransaction(transaction: code.management.ImporterAPI.ImporterTransaction): net.liftweb.common.Box[code.model.Transaction] = ???
  def createSandboxBankAccount(bankId: code.model.BankId,accountId: code.model.AccountId,accountNumber: String,currency: String,initialBalance: BigDecimal,accountHolderName: String): net.liftweb.common.Box[code.model.BankAccount] = ???
  protected def createTransactionRequestImpl(transactionRequestId: code.model.TransactionRequestId,transactionRequestType: code.model.TransactionRequestType,fromAccount: code.model.BankAccount,counterparty: code.model.BankAccount,body: code.transactionrequests.TransactionRequests.TransactionRequestBody,status: String): net.liftweb.common.Box[code.transactionrequests.TransactionRequests.TransactionRequest] = ???
  def getAccountByUUID(uuid: String): net.liftweb.common.Box[code.bankconnectors.KafkaConnector.AccountType] = ???
  def getAccountHolders(bankId: code.model.BankId,accountID: code.model.AccountId): Set[code.model.User] = ???
  protected def getBankAccountType(bankId: code.model.BankId,accountId: code.model.AccountId): net.liftweb.common.Box[code.bankconnectors.KafkaConnector.AccountType] = ???
  def getMatchingTransactionCount(bankNationalIdentifier: String,accountNumber: String,amount: String,completed: java.util.Date,otherAccountHolder: String): Int = ???
  def getOtherBankAccount(bankId: code.model.BankId,accountID: code.model.AccountId,otherAccountID: String): net.liftweb.common.Box[code.model.OtherBankAccount] = ???
  def getOtherBankAccounts(bankId: code.model.BankId,accountID: code.model.AccountId): List[code.model.OtherBankAccount] = ???
  def getPhysicalCards(user: code.model.User): Set[code.model.PhysicalCard] = ???
  def getPhysicalCardsForBank(bankId: code.model.BankId,user: code.model.User): Set[code.model.PhysicalCard] = ???
  def getTransaction(bankId: code.model.BankId,accountID: code.model.AccountId,transactionId: code.model.TransactionId): net.liftweb.common.Box[code.model.Transaction] = ???
  protected def getTransactionRequestImpl(transactionRequestId: code.model.TransactionRequestId): net.liftweb.common.Box[code.transactionrequests.TransactionRequests.TransactionRequest] = ???
  protected def getTransactionRequestTypesImpl(fromAccount: code.model.BankAccount): net.liftweb.common.Box[List[code.model.TransactionRequestType]] = ???
  protected def getTransactionRequestsImpl(fromAccount: code.model.BankAccount): net.liftweb.common.Box[List[code.transactionrequests.TransactionRequests.TransactionRequest]] = ???
  protected def makePaymentImpl(fromAccount: code.bankconnectors.KafkaConnector.AccountType,toAccount: code.bankconnectors.KafkaConnector.AccountType,amt: BigDecimal,description: String): net.liftweb.common.Box[code.model.TransactionId] = ???
  def removeAccount(bankId: code.model.BankId,accountId: code.model.AccountId): Boolean = ???
  protected def saveTransactionRequestChallengeImpl(transactionRequestId: code.model.TransactionRequestId,challenge: code.transactionrequests.TransactionRequests.TransactionRequestChallenge): net.liftweb.common.Box[Boolean] = ???
  protected def saveTransactionRequestStatusImpl(transactionRequestId: code.model.TransactionRequestId,status: String): net.liftweb.common.Box[Boolean] = ???
  protected def saveTransactionRequestTransactionImpl(transactionRequestId: code.model.TransactionRequestId,transactionId: code.model.TransactionId): net.liftweb.common.Box[Boolean] = ???
  def setAccountHolder(bankAccountUID: code.model.BankAccountUID,user: code.model.User): Unit = ???
  def setBankAccountLastUpdated(bankNationalIdentifier: String,accountNumber: String,updateDate: java.util.Date): Boolean = ???
  def updateAccountBalance(bankId: code.model.BankId,accountId: code.model.AccountId,newBalance: BigDecimal): Boolean = ???
  def updateAccountLabel(bankId: code.model.BankId,accountId: code.model.AccountId,label: String): Boolean = ???
}


object ZooKeeperUtils {

  // gets brokers tracked by zookeeper
  def getBrokers(zookeeper:String): List[String] = {
    val zkClient = new ZkClient(zookeeper, 30000, 30000, ZKStringSerializer)
    val brokers = for {broker <- ZkUtils.getAllBrokersInCluster(zkClient) } yield {
      broker.host +":"+ broker.port
    }
    zkClient.close()
    brokers.toList
  }

  // gets all topics tracked by zookeeper
  def getTopics(zookeeper:String): List[String] = {
    val zkClient = new ZkClient(zookeeper, 30000, 30000, ZKStringSerializer)
    val res = ZkUtils.getAllTopics(zkClient).toList
    zkClient.close()
    res
  }

}

class KafkaConsumer(val zookeeper: String,
                    val groupId: String,
                    val topic: String,
                    val delay: Long) {

  val config = createConsumerConfig(zookeeper, groupId)
  val consumer = Consumer.create(config)

  def shutdown() = {
    if (consumer != null)
      consumer.shutdown()
  }
  def createConsumerConfig(zookeeper: String, groupId: String): ConsumerConfig = {
    val props = new Properties()
    props.put("zookeeper.connect", zookeeper)
    props.put("group.id", groupId)
    props.put("auto.offset.reset", "largest")
    props.put("zookeeper.session.timeout.ms", "400")
    props.put("zookeeper.sync.time.ms", "400")
    props.put("auto.commit.interval.ms", "800")
    props.put("consumer.timeout.ms", "5000")
    val config = new ConsumerConfig(props)
    config
  }
  def getResponse(reqId: String): Map[String, String] = {
    val topicCountMap = Map(topic -> 1)
    val consumerMap = consumer.createMessageStreams(topicCountMap)
    val streams = consumerMap.get(topic).get
    for (stream <- streams) {
      val it = stream.iterator()
      try {
        while (it.hasNext()) {
          val m = it.next()
          val msg = new String(m.message(), "UTF8")
          val key = new String(m.key(), "UTF8")
          if (key == reqId) {
            shutdown()
            val p = """"(\w*?)":"(.*?)"""".r
            val r = (for( p(k, v) <- p.findAllIn(msg) ) yield  (k -> v)).toMap[String, String]
            return r;
          }
        }
      }
      catch {
        case e:kafka.consumer.ConsumerTimeoutException => println("Exception: " + e.toString())
        shutdown()
        return Map("error" -> "timeout")
      }
    }
    shutdown()
    return Map("" -> "")
  }
}


case class KafkaProducer(
                          topic: String,
                          brokerList: String,
                          clientId: String = UUID.randomUUID().toString,
                          synchronously: Boolean = true,
                          compress: Boolean = true,
                          batchSize: Integer = 200,
                          messageSendMaxRetries: Integer = 3,
                          requestRequiredAcks: Integer = -1
                          //requestRequiredAcks: Integer = 1
                          ) {

  val props = new Properties()

  val codec = if (compress) DefaultCompressionCodec.codec else NoCompressionCodec.codec

  props.put("compression.codec", codec.toString)
  props.put("producer.type", if (synchronously) "sync" else "async")
  props.put("metadata.broker.list", brokerList)
  props.put("batch.num.messages", batchSize.toString)
  props.put("message.send.max.retries", messageSendMaxRetries.toString)
  props.put("request.required.acks", requestRequiredAcks.toString)
  props.put("client.id", clientId.toString)

  val producer = new Producer[AnyRef, AnyRef](new ProducerConfig(props))

  def kafkaMesssage(key: Array[Byte], message: Array[Byte], partition: Array[Byte]): KeyedMessage[AnyRef, AnyRef] = {
    if (partition == null) {
      new KeyedMessage(topic, key, message)
    } else {
      new KeyedMessage(topic, partition, key, message)
    }
  }

  def send(key: String, message: String, partition: String = null): Unit = send(key.getBytes("UTF8"), message.getBytes("UTF8"), if (partition == null) null else partition.getBytes("UTF8"))

  def send(key: Array[Byte], message: Array[Byte], partition: Array[Byte]): Unit = {
    try {
      producer.send(kafkaMesssage(key, message, partition))
    } catch {
      case e: Exception =>
        e.printStackTrace()
    }
  }
}