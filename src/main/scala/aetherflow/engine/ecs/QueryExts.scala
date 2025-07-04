package aetherflow.engine.ecs

import zio.*
import zio.stm.*

import scala.collection.Seq
import scala.reflect.*
import aetherflow.engine.ecs.Data.*
import aetherflow.engine.utils.Types.*
import aetherflow.engine.utils.Extensions.*

trait QueryExts
object QueryExts {

  extension (world: EcsStateMachine) {
    def query1[
      A1 <: Component : ClassTag,
    ]: UIO[Seq[(Entity, CompRef[A1])]] =
      world.queryGeneric(classTag[A1]).flatMap { allEntities =>
        ZIO.foreach(allEntities) { case (entity, entityComponents) =>
          val transaction = for {
            a1 <- entityComponents.head.narrowComp[A1].getOrThrow
          } yield (entity, a1)

          // Lock only single entities components at once
          transaction.commit
        }
      }

    def query2[
      A1 <: Component : ClassTag,
      A2 <: Component : ClassTag,
    ]: UIO[Seq[(Entity, CompRef[A1], CompRef[A2])]] =
      world.queryGeneric(classTag[A1], classTag[A2]).flatMap { allEntities =>
        ZIO.foreach(allEntities) { case (entity, entityComponents) =>
          val transaction = for {
            a1 <- entityComponents.head.narrowComp[A1].getOrThrow
            a2 <- entityComponents(1).narrowComp[A2].getOrThrow
          } yield (entity, a1, a2)

          // Lock only single entities components at once
          transaction.commit
        }
      }

    def query3[
      A1 <: Component : ClassTag,
      A2 <: Component : ClassTag,
      A3 <: Component : ClassTag,
    ]: UIO[Seq[(Entity, CompRef[A1], CompRef[A2], CompRef[A3])]] =
      world.queryGeneric(classTag[A1], classTag[A2], classTag[A3]).flatMap { allEntities =>
        ZIO.foreach(allEntities) { case (entity, entityComponents) =>
          val transaction = for {
            a1 <- entityComponents.head.narrowComp[A1].getOrThrow
            a2 <- entityComponents(1).narrowComp[A2].getOrThrow
            a3 <- entityComponents(2).narrowComp[A3].getOrThrow
          } yield (entity, a1, a2, a3)

          // Lock only single entities components at once
          transaction.commit
        }
      }
  }
}
