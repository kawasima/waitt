module Page.Thread exposing (Model, Msg, init, update, view)

import Api
import Api.Endpoint as Endpoint
import Browser.Dom as Dom
import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (..)
import Http
import Page
import Route exposing (Route)
import Task exposing (Task)
import Log

import Model.ThreadDump exposing (ThreadDump, ThreadInfo, StackTraceElement)

-- MODEL

type alias Model =
    { threadDump : Status ThreadDump
    }

type Status a
    = Loading
    | Loaded a
    | Failed

init : ( Model, Cmd Msg )
init =
    ( { threadDump = Loading
      }
    , Task.attempt CompletedThreadDumpLoad fetchThreadDump
    )

-- VIEW

view : Model -> { title : String, content : Html Msg }
view model =
    { title = "Thread dump / WAITT dashboard"
    , content =
        case model.threadDump of
            Loading -> div [ ] [ text "loading..." ]
            Loaded threadDump -> viewThreadDump threadDump
            Failed  -> div [ ] [ text "loading failed" ]
    }

viewThreadDump : ThreadDump -> Html Msg
viewThreadDump threadDump =
    div [ class "three-forths column" ]
        [ h1 [ ]
              [ i [ class "meta-octicon octico-dashboard" ] [ ]
              , text "Thread Dump"
              ]
        , div [ ]
            (List.map viewThreadInfo threadDump.threads)
        ]

viewThreadInfo : ThreadInfo -> Html Msg
viewThreadInfo threadInfo =
    div []
        [ pre []
              [ text ("\"" ++ threadInfo.threadName ++ "\"" ++
                     "\n    java.lang.Thread.State: " ++ threadInfo.threadState ++
                     (String.join "\n        at" (List.map viewStackTraceElement threadInfo.stackTrace))) ]
        ]

viewStackTraceElement : StackTraceElement -> String
viewStackTraceElement stackTraceElement =
    stackTraceElement.declaringClass ++ "." ++ stackTraceElement.methodName ++
        " (" ++ stackTraceElement.fileName ++ ":" ++ String.fromInt stackTraceElement.lineNumber ++ ")"
-- UPDATE

type Msg
    = CompletedThreadDumpLoad (Result Http.Error ThreadDump)

update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        CompletedThreadDumpLoad (Ok threadDump) ->
            ( { model | threadDump = Loaded threadDump }, Cmd.none )
        CompletedThreadDumpLoad (Err error) ->
            ( { model | threadDump = Failed }
            , Log.error
            )

-- HTTP

fetchThreadDump : Task Http.Error ThreadDump
fetchThreadDump =
    let
        decoder =
            Model.ThreadDump.decoder
        query = [ ]
    in
        Http.task
            { method = "GET"
            , headers = Api.headers
            , url = Api.url ["thread"] query
            , body = Http.emptyBody
            , resolver = Api.jsonResolver decoder
            , timeout = Nothing
            }

-- SUBSCRIPTION

subscriptions : Model -> Sub Msg
subscriptions model =
    Sub.none
