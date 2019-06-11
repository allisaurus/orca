/*
 * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.netflix.spinnaker.orca.clouddriver.tasks.providers.ecs

import com.google.common.collect.Maps
import spock.lang.Specification
import spock.lang.Subject
import com.netflix.spinnaker.orca.pipeline.model.Execution
import com.netflix.spinnaker.orca.pipeline.model.Execution.ExecutionType
import static com.netflix.spinnaker.orca.test.model.ExecutionBuilder.stage

class EcsServerGroupCreatorSpec extends Specification {

  @Subject
  def creator = new EcsServerGroupCreator()
  def stage = stage {}

  def deployConfig = [
    credentials: "testUser",
    application: "ecs"
  ]

  def setup() {
    stage.execution.stages.add(stage)
    stage.context = deployConfig
  }

  def cleanup() {
    stage.execution.stages.clear()
    stage.execution.stages.add(stage)
  }

  def "creates operation from trigger image"() {
    given:
    def (testReg,testRepo,testTag) = ["myregistry.io","myrepo","latest"]
    def testDescription = [
      fromTrigger: "true",
      registry: testReg,
      repository: testRepo,
      tag: testTag
    ]
    stage.context.imageDescription = testDescription
    stage.execution = new Execution(ExecutionType.PIPELINE, 'ecs')
    def expected = Maps.newHashMap(deployConfig)
    expected.dockerImageAddress = "$testReg/$testRepo:$testTag"

    when:
    def operations = creator.getOperations(stage)

    then:
    operations.find {
      it.containsKey("createServerGroup")
    }.createServerGroup == expected
  }
}
