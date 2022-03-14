package com.quadstingray.mongo.camp.model.auth

import sttp.tapir.model.UsernamePassword

case class AuthInputBasic(basic: Option[UsernamePassword]) extends AuthInput
