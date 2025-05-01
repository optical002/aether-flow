package engine.ecs

import scala.collection.Seq
import scala.reflect.{ClassTag, classTag}

trait QueryExts
object QueryExts {
  import engine.ecs.Data.*
  extension (world: World) {
    def query1[A <: Component : ClassTag]: Seq[(Entity, A)] = {
      world.queryGeneric(classTag[A]).map { case (e, seq) =>
        (e, seq.head.asInstanceOf[A])
      }
    }

    def query2[
      A1 <: Component : ClassTag,
      A2 <: Component : ClassTag,
    ]: Seq[(Entity, A1, A2)] = {
      world.queryGeneric(classTag[A1], classTag[A2]).map { case (e, seq) =>
        (e, seq.head.asInstanceOf[A1], seq(1).asInstanceOf[A2])
      }
    }

    def query3[
      A1 <: Component : ClassTag,
      A2 <: Component : ClassTag,
      A3 <: Component : ClassTag,
    ]: Seq[(Entity, A1, A2, A3)] = {
      world.queryGeneric(classTag[A1], classTag[A2], classTag[A3]).map { case (e, seq) =>
        (e, seq.head.asInstanceOf[A1], seq(1).asInstanceOf[A2], seq(2).asInstanceOf[A3])
      }
    }

    def query4[
      A1 <: Component : ClassTag,
      A2 <: Component : ClassTag,
      A3 <: Component : ClassTag,
      A4 <: Component : ClassTag,
    ]: Seq[(Entity, A1, A2, A3, A4)] = {
      world.queryGeneric(classTag[A1], classTag[A2], classTag[A3], classTag[A4]).map { case (e, seq) =>
        (e, seq.head.asInstanceOf[A1], seq(1).asInstanceOf[A2], seq(2).asInstanceOf[A3], seq(3).asInstanceOf[A4])
      }
    }

    def query5[
      A1 <: Component : ClassTag,
      A2 <: Component : ClassTag,
      A3 <: Component : ClassTag,
      A4 <: Component : ClassTag,
      A5 <: Component : ClassTag,
    ]: Seq[(Entity, A1, A2, A3, A4, A5)] = {
      world.queryGeneric(classTag[A1], classTag[A2], classTag[A3], classTag[A4], classTag[A5]).map { case (e, seq) =>
        (e, seq.head.asInstanceOf[A1], seq(1).asInstanceOf[A2], seq(2).asInstanceOf[A3], seq(3).asInstanceOf[A4],
          seq(4).asInstanceOf[A5])
      }
    }

    def query6[
      A1 <: Component : ClassTag,
      A2 <: Component : ClassTag,
      A3 <: Component : ClassTag,
      A4 <: Component : ClassTag,
      A5 <: Component : ClassTag,
      A6 <: Component : ClassTag,
    ]: Seq[(Entity, A1, A2, A3, A4, A5, A6)] = {
      world.queryGeneric(classTag[A1], classTag[A2], classTag[A3], classTag[A4], classTag[A5], classTag[A6]).map { case (e, seq) =>
        (e, seq.head.asInstanceOf[A1], seq(1).asInstanceOf[A2], seq(2).asInstanceOf[A3], seq(3).asInstanceOf[A4],
          seq(4).asInstanceOf[A5], seq(5).asInstanceOf[A6])
      }
    }

    def query7[
      A1 <: Component : ClassTag,
      A2 <: Component : ClassTag,
      A3 <: Component : ClassTag,
      A4 <: Component : ClassTag,
      A5 <: Component : ClassTag,
      A6 <: Component : ClassTag,
      A7 <: Component : ClassTag,
    ]: Seq[(Entity, A1, A2, A3, A4, A5, A6, A7)] = {
      world.queryGeneric(
        classTag[A1], classTag[A2], classTag[A3], classTag[A4], classTag[A5], classTag[A6], classTag[A7]
      ).map { case (e, seq) =>
        (e, seq.head.asInstanceOf[A1], seq(1).asInstanceOf[A2], seq(2).asInstanceOf[A3], seq(3).asInstanceOf[A4],
          seq(4).asInstanceOf[A5], seq(5).asInstanceOf[A6], seq(6).asInstanceOf[A7])
      }
    }

    def query8[
      A1 <: Component : ClassTag,
      A2 <: Component : ClassTag,
      A3 <: Component : ClassTag,
      A4 <: Component : ClassTag,
      A5 <: Component : ClassTag,
      A6 <: Component : ClassTag,
      A7 <: Component : ClassTag,
      A8 <: Component : ClassTag,
    ]: Seq[(Entity, A1, A2, A3, A4, A5, A6, A7, A8)] = {
      world.queryGeneric(
        classTag[A1], classTag[A2], classTag[A3], classTag[A4], classTag[A5], classTag[A6], classTag[A7], classTag[A8]
      ).map { case (e, seq) =>
        (e, seq.head.asInstanceOf[A1], seq(1).asInstanceOf[A2], seq(2).asInstanceOf[A3], seq(3).asInstanceOf[A4],
          seq(4).asInstanceOf[A5], seq(5).asInstanceOf[A6], seq(6).asInstanceOf[A7], seq(7).asInstanceOf[A8])
      }
    }

    def query9[
      A1 <: Component : ClassTag,
      A2 <: Component : ClassTag,
      A3 <: Component : ClassTag,
      A4 <: Component : ClassTag,
      A5 <: Component : ClassTag,
      A6 <: Component : ClassTag,
      A7 <: Component : ClassTag,
      A8 <: Component : ClassTag,
      A9 <: Component : ClassTag,
    ]: Seq[(Entity, A1, A2, A3, A4, A5, A6, A7, A8, A9)] = {
      world.queryGeneric(
        classTag[A1], classTag[A2], classTag[A3], classTag[A4], classTag[A5], classTag[A6], classTag[A7], classTag[A8],
        classTag[A9]
      ).map { case (e, seq) =>
        (e, seq.head.asInstanceOf[A1], seq(1).asInstanceOf[A2], seq(2).asInstanceOf[A3], seq(3).asInstanceOf[A4],
          seq(4).asInstanceOf[A5], seq(5).asInstanceOf[A6], seq(6).asInstanceOf[A7], seq(7).asInstanceOf[A8],
          seq(8).asInstanceOf[A9])
      }
    }

    def query10[
      A1 <: Component : ClassTag,
      A2 <: Component : ClassTag,
      A3 <: Component : ClassTag,
      A4 <: Component : ClassTag,
      A5 <: Component : ClassTag,
      A6 <: Component : ClassTag,
      A7 <: Component : ClassTag,
      A8 <: Component : ClassTag,
      A9 <: Component : ClassTag,
      A10 <: Component : ClassTag,
    ]: Seq[(Entity, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10)] = {
      world.queryGeneric(
        classTag[A1], classTag[A2], classTag[A3], classTag[A4], classTag[A5], classTag[A6], classTag[A7], classTag[A8],
        classTag[A9], classTag[A10]
      ).map { case (e, seq) =>
        (e, seq.head.asInstanceOf[A1], seq(1).asInstanceOf[A2], seq(2).asInstanceOf[A3], seq(3).asInstanceOf[A4],
          seq(4).asInstanceOf[A5], seq(5).asInstanceOf[A6], seq(6).asInstanceOf[A7], seq(7).asInstanceOf[A8],
          seq(8).asInstanceOf[A9], seq(9).asInstanceOf[A10])
      }
    }
  }
}
