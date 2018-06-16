

import java.util.concurrent.{CompletionStage}

import software.amazon.awssdk.services.batch._
import software.amazon.awssdk.services.batch.model.{ListJobsRequest, ListJobsResponse}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.java8.FuturesConvertersImpl._

// https://www.ducons.com/blog/tests-and-thoughts-on-asynchronous-io-vs-multithreading

object Hello extends App {

  def toScala[T](cs: CompletionStage[T]): Future[T] = {
    cs match {
      case cf: CF[T] => cf.wrapped
      case _ =>
        val p = new P[T](cs)
        cs whenComplete p
        p.future
    }
  }

  println("Hello, World!")

  // https://www.javatips.net/api/aws-sdk-java-master/aws-java-sdk-batch/src/main/java/com/amazonaws/services/batch/AWSBatchAsyncClientBuilder.java

  // https://aws.amazon.com/blogs/developer/aws-sdk-for-java-2-0-developer-preview/

  val batchClient = BatchAsyncClient.create()
  val listJobsRequest = ListJobsRequest.builder()
    .jobQueue("blue")
    .build()
  val opResult: Future[ListJobsResponse] = toScala(batchClient.listJobs(listJobsRequest))

  Await.result(opResult, 1 minute)
  println(opResult.value)

}
