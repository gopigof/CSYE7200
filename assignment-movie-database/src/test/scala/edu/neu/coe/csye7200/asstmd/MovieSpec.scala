package edu.neu.coe.csye7200.asstmd

import edu.neu.coe.csye7200.asstmd.Movie.{sequenceOptimistic, sequencePessimistic}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scala.io.Source
import scala.util.Try

/**
  * Created by scalaprof on 9/13/16.
  */
class MovieSpec extends AnyFlatSpec with Matchers {

  private val phi = (math.sqrt(5) + 1) / 2

  behavior of "Name"

  it should "work for String" in {
    val x = Name("Tom Brady")
    x should matchPattern {
      case Name("Tom", None, "Brady", None) =>
    }
    Name("Noémie Lenoir") should matchPattern {
      case Name("Noémie", None, "Lenoir", None) =>
    }
    Name("J.J. Abrams") should matchPattern {
      case Name("J.", Some("J."), "Abrams", None) =>
    }
    Name("Robert Downey Jr.") should matchPattern {
      case Name("Robert", None, "Downey", Some("Jr.")) =>
    }
  }
  it should "work for Name" in {
    val x = Name("Tom", None, "Brady", None)
    x should matchPattern {
      case Name("Tom", None, "Brady", None) =>
    }
  }

  behavior of "Principal"

  it should "work for String, Int" in {
    val x = Principal("Tom Brady", 1)
    x should matchPattern { case Principal(Name("Tom", None, "Brady", None), 1) => }
  }
  it should "work for List[String]" in {
    Principal(List("Tom Brady", "1")) should matchPattern {
      case Principal(Name("Tom", None, "Brady", None), 1) =>
    }
    Principal(List("Noémie Lenoir", "2")) should matchPattern {
      case Principal(Name("Noémie", None, "Lenoir", None), 2) =>
    }
    Principal(List("J.J. Abrams", "3")) should matchPattern {
      case Principal(Name("J.", Some("J."), "Abrams", None), 3) =>
    }
    Principal(List("Robert Downey Jr.", "4")) should matchPattern {
      case Principal(Name("Robert", None, "Downey", Some("Jr.")), 4) =>
    }

  }

  behavior of "Rating"

  it should "work for String, Int" in {
    val x = Rating("PG", Some(13))
    x should matchPattern {
      case Rating("PG", Some(13)) =>
    }
  }
  it should "work for PG-13" in {
    val x = Rating("PG-13")
    x should matchPattern {
      case Rating("PG", Some(13)) =>
    }
  }
  it should "work for R" in {
    val x = Rating("R")
    x should matchPattern {
      case Rating("R", None) =>
    }
  }
  it should "work for PG-0" in {
    an[Exception] should be thrownBy Rating("PG-XX")
  }
  it should "work for PG-XX" in {
    an[Exception] should be thrownBy Rating("PG-XX")
  }
  it should "work for null" in {
    an[Exception] should be thrownBy Rating(null)
  }
  it should "work for NULL" in {
    an[Exception] should be thrownBy Rating("NULL")
  }
  it should "work for Approved" in {
    an[Exception] should be thrownBy Rating("Approved")
  }
  it should "work for Not Rated" in {
    an[Exception] should be thrownBy Rating("Not Rated")
  }

  behavior of "Format"

  it should "work for Boolean, String, Double, Int" in {
    val x = Format(color = true, "Swahili", phi, 129)
    x should matchPattern {
      case Format(true, "Swahili", `phi`, 129) =>
    }
  }
  it should "work for List[String]" in {
    val x = Format(List("Color", "Swahili", phi.toString, "129"))
    x should matchPattern {
      case Format(true, "Swahili", `phi`, 129) =>
    }
  }

  behavior of "Production"

  it should "work for String, Int" in {
    val x = Production("Kenya", 1000000, 1000001, 2016)
    x should matchPattern {
      case Production("Kenya", 1000000, 1000001, 2016) =>
    }
  }
  it should "work for List[String]" in {
    val x = Production(List("Kenya", "1000000", "1000001", "2016"))
    x should matchPattern {
      case Production("Kenya", 1000000, 1000001, 2016) =>
    }
  }
  it should "define isKiwi properly" in {
    Production("Kenya", 1000000, 1000001, 2016).isKiwi shouldBe false
    Production("New Zealand", 1000000, 1000001, 2016).isKiwi shouldBe true
  }

  behavior of "Reviews"

  it should "work for params" in {
    val x = Reviews(8.14, 42, Rating("PG-13"), 7, 10, 12, 99)
    x should matchPattern {
      case Reviews(8.14, 42, Rating("PG", Some(13)), 7, 10, 12, 99) =>
    }
  }
  it should "work for List[String]" in {
    val x = Reviews(List("8.14", "42", "PG-13", "7", "10", "12", "99"))
    x should matchPattern {
      case Reviews(8.14, 42, Rating("PG", Some(13)), 7, 10, 12, 99) =>
    }
  }

  behavior of "sequence"
  it should "do sequenceOptimistic 1" in {
    lazy val ingester = new Ingest[Movie]()
    val source = Source.fromFile("assignment-movie-database/src/test/resources/movie_metadata.csv")
    val triedMovies: Iterator[Try[Movie]] = for (my <- ingester(source)) yield for (m <- my; if m.production.isKiwi) yield m
    val optionalMovies: Seq[Option[Movie]] = triedMovies to List map (_.toOption)
    val kiwiMovies: Option[Seq[Movie]] = sequenceOptimistic(optionalMovies)
    source.close()
    kiwiMovies.getOrElse(Nil).size shouldBe 4
  }
  it should "do sequenceOptimistic 2" in {
    lazy val ingester = new Ingest[Movie]()
    val source = Source.fromFile("assignment-movie-database/src/test/resources/movie_metadata.csv")
    val triedMovies: Iterator[Try[Movie]] = for (my <- ingester(source)) yield for (m <- my; if m.production.isCheap) yield m
    val optionalMovies: Seq[Option[Movie]] = triedMovies to List map (_.toOption)
    val cheapMovies: Option[Seq[Movie]] = sequenceOptimistic(optionalMovies)
    source.close()
    cheapMovies shouldBe None
  }
  it should "do sequencePessimistic 1" in {
    lazy val ingester = new Ingest[Movie]()
    val source = Source.fromFile("assignment-movie-database/src/test/resources/movie_metadata.csv")
    val triedMovies: Iterator[Try[Movie]] = for (my <- ingester(source)) yield for (m <- my; if m.production.isKiwi) yield m
    val optionalMovies: Seq[Option[Movie]] = triedMovies to List map (_.toOption)
    val kiwiMovies: Option[Seq[Movie]] = sequencePessimistic(optionalMovies)
    source.close()
    kiwiMovies shouldBe None
  }
  it should "do sequencePessimistic 2" in {
    lazy val ingester = new Ingest[Movie]()
    val source = Source.fromFile("assignment-movie-database/src/test/resources/movie_metadata.csv")
    val triedMovies: Iterator[Try[Movie]] = for (my <- ingester(source)) yield for (m <- my) yield m
    val optionalMovies: Seq[Option[Movie]] = triedMovies to List map (_.toOption) filter (_.isDefined)
    val maybeMovies: Option[Seq[Movie]] = sequencePessimistic(optionalMovies)
    source.close()
    maybeMovies should matchPattern { case Some(xs) => }
    maybeMovies.get.size shouldBe 1480
  }
}