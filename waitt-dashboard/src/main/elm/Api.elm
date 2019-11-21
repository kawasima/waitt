port module Api exposing (url, headers, jsonResolver)

import Url.Builder exposing (QueryParameter, string)
import Browser
import Browser.Navigation as Nav
import Http exposing (Body, Expect)
import Json.Decode as Decode exposing (Decoder, Value, decodeString, field, string, map)
import Json.Decode.Pipeline as Pipeline exposing (optional, required)
import Json.Encode as Encode
import Url exposing (Url)

-- URL

url : List String -> List QueryParameter -> String
url paths queryParams =
    Url.Builder.crossOrigin "http://localhost:8080"
        ("_dashboard" :: paths)
        queryParams

--

headers : List Http.Header
headers =
    List.concat
        [
         [ Http.header "accept" "application/json"
         ]
        ]

-- HTTP

jsonResolver : Decoder a -> Http.Resolver Http.Error a
jsonResolver decoder =
    Http.stringResolver <|
        \response ->
            case response of
                Http.BadUrl_ u ->
                    Err (Http.BadUrl u)

                Http.Timeout_ ->
                    Err Http.Timeout

                Http.BadStatus_ { statusCode } _ ->
                    Err (Http.BadStatus statusCode)

                Http.NetworkError_ ->
                    Err Http.NetworkError

                Http.GoodStatus_ _ body ->
                    case Decode.decodeString decoder body of
                        Ok value ->
                            Ok value

                        Err err ->
                            Err (Http.BadBody (Decode.errorToString err))

-- PERSISTENCE

port onStoreChange : (Value -> msg) -> Sub msg
port storeCache : Maybe Value -> Cmd msg
