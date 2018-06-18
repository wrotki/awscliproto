

import java.util
import java.util.concurrent.CompletionStage

import software.amazon.awssdk.services.batch._
import software.amazon.awssdk.services.batch.model.{JobSummary, ListJobsRequest, ListJobsResponse}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.java8.FuturesConvertersImpl._
import collection.JavaConverters._
import scala.util.Try

// https://www.ducons.com/blog/tests-and-thoughts-on-asynchronous-io-vs-multithreading

object Hello extends App {

  val batchClient = BatchAsyncClient.create()

  case class JobList(
                      nextToken: String,
                      jobList: Seq[JobSummary]
                    )

  def listAllJobs(): Unit = {

    val first: Future[JobList] = listPageOfJobs(first = true, nextToken = null)

    def loop(nt: String, currList: Seq[JobList]): Future[Seq[JobList]] = {
      listPageOfJobs(first = false, nextToken = nt) flatMap { sjl =>
        sjl.nextToken match {
          case null => Future(currList :+ sjl)
          case nxt => loop(sjl.nextToken, currList :+ sjl)
        }
      }
    }

    val listFuture: Future[Seq[JobList]] = first flatMap { jl =>
      jl.nextToken match {
        case null => Future(Seq(jl))
        case nxt => loop(jl.nextToken, Seq(jl))
      }
    }

    Await.result(listFuture, 1 minute)
    val list = listFuture.value.get.get
    val len = list.length
    val lfirst = list.head

    println(s"list length: $lfirst")
  }

  //  http://aws-java-sdk-javadoc.s3-website-us-west-2.amazonaws.com/latest/overview-summary.html
  //  http://aws-java-sdk-javadoc.s3-website-us-west-2.amazonaws.com/latest/software/amazon/awssdk/services/batch/BatchAsyncClient.html
  //  https://docs.aws.amazon.com/batch/latest/APIReference/API_ListJobs.html

  def listPageOfJobs(first: Boolean, nextToken: String): Future[JobList] = {

    val baseListJobsRequest = ListJobsRequest.builder()
      .jobQueue("blue")

    val listJobsRequest =
      if (first) {
        baseListJobsRequest
      }
      else {
        baseListJobsRequest.nextToken(nextToken)
      }

    val opResult = toScala(batchClient.listJobs(listJobsRequest.build()))
    val res: Future[JobList] = opResult flatMap { ljr =>
      val nxt = ljr.nextToken()
      val seq = ljr.jobSummaryList().asScala.toSeq
      Future(JobList(nxt, seq))
    }
    res
  }

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
  listAllJobs

  // https://www.javatips.net/api/aws-sdk-java-master/aws-java-sdk-batch/src/main/java/com/amazonaws/services/batch/AWSBatchAsyncClientBuilder.java

  // https://aws.amazon.com/blogs/developer/aws-sdk-for-java-2-0-developer-preview/

  //  val listJobsRequest = ListJobsRequest.builder()
  //    .jobQueue("blue")
  //    .build()
  //  val opResult: Future[ListJobsResponse] = toScala(batchClient.listJobs(listJobsRequest))
  //
  //  Await.result(opResult, 1 minute)
  //  println(opResult.value)
  //
  //  val next = opResult.value.get.get.nextToken()

}
