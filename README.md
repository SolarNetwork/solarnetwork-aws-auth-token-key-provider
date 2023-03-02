# SolarNetwork Example AWS Lamda Security Token Signing Key Provider

This project contains a Java AWS Lamda function that, when invoked, returns a signing key derived
from a [SolarNetwork token secret][sn-auth].

The purpose of this Lamda function is to provide a token signing key to clients, without exposing
the actual token secret. This implementation serves as an example only.

Once deployed to AWS as a Lamda function, you can configure a Function URL to allow calling the
function at an HTTP URL, or you could integrate it as an AWS API Gateway proxy handler. You would
then give the SolarNetwork token to the client. The client would then invoke the function URL to
obtain the signing key that can then be used to sign SolarNetwork API requests.

> :warning: Note how the token secret configured on the Lamda must be for the token you give to the
> client!

The function returns a simple JSON object like this:

```json
{
  "success": true,
  "key": "6492162046e769f9a3468cbc1992c92003233cf81c507fae5e39cd23674d2786",
  "date": "20230301"
}
```

Those properties are described as:

| Property | Description |
|:---------|:------------|
| `success` | Will be `true` if the function succeeded, `false` otherwise. |
| `message` | If an error occurred, a reason will be provided. |
| `key`     | The hex-encoded token signing key. |
| `date`    | The `YYYYMMDD` date used in the signing key. This is based on the time that the function executes. |

# Building

You can build a `.zip` archive suitable for uploading to an AWS Lamda function by running

```
# Posix
./gradlew build

# or, Windows
.\gradlew.bat build
```

The archive will be built to `app/build/distributions/s10k-aws-lamda-token-key-provider.zip`.

# Deploying

The following Lamda configuration is necessary:

| Setting | Value |
|:--------|:------|
| Runtime | Java 11 |
| Handler | `net.s10k.aws.lamda.security.keyprovider.TokenKeyProvider` |

The following environment variables must be configured:

| Variable | Description |
|:---------|:------------|
| `SN_TOKEN_SECRET` | The token secret to provide signing keys for. |


[sn-auth]: https://github.com/SolarNetwork/solarnetwork/wiki/SolarNet-API-authentication-scheme-V2
