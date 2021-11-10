/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.engineconn.acessible.executor.operator

import org.apache.linkis.common.exception.WarnException
import org.apache.linkis.common.utils.{ClassUtils, Logging}
import org.apache.linkis.manager.common.protocol.engine.EngineOperateRequest

trait OperatorFactory {

  def createOperatorRequest(request: EngineOperateRequest): Operator

}

object OperatorFactory {

  private val operatorFactory = new OperatorFactoryImpl

  def apply(): OperatorFactory = operatorFactory

}

import scala.collection.convert.WrapAsScala._
class OperatorFactoryImpl extends OperatorFactory with Logging {

  private val operators: Map[String, _ <: Operator] = ClassUtils.reflections.getSubTypesOf(classOf[Operator])
    .filterNot(ClassUtils.isInterfaceOrAbstract).map { clazz =>
      val operator = clazz.newInstance()
    operator.getName -> operator
    }.toMap
  info("Launched operators list => " + operators)

  override def createOperatorRequest(request: EngineOperateRequest): Operator = {
    request.parameters.getOrElse(EngineOperateRequest.OPERATOR_NAME_KEY,
      throw new WarnException(20031, s"${EngineOperateRequest.OPERATOR_NAME_KEY} is not exists.")) match {
      case operatorName: String if operators.contains(operatorName) =>
        operators(operatorName)
      case operatorName => throw new WarnException(20030, s"Cannot find operator named $operatorName.")
    }
  }

}
