module Page.Server exposing (Model, Msg, init, update, view)

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

import Model.Server as Server exposing (Server)

-- MODEL

type alias Model =
    { server : Status Server
    }

type Status a
    = Loading
    | Loaded a
    | Failed

init : ( Model, Cmd Msg )
init =
    ( { server = Loading
      }
    , Task.attempt CompletedServerLoad fetchServer
    )

-- VIEW

view : Model -> { title : String, content : Html Msg }
view model =
    { title = "Server"
    , content =
        case model.server of
            Loading -> div [ ] [ text "loading..." ]
            Loaded server -> viewServer server
            Failed  -> div [ ] [ text "loading failed" ]
    }

viewServer : Server -> Html Msg
viewServer server =
    div [ class "three-forths column" ]
        [ h1 [ ]
              [ i [ class "meta-octicon octico-dashboard" ] [ ]
              , text "Server"
              ]
        , div [ class "card" ]
            [ ul [ class "list-group list-group-flush" ]
              [ li [ class "list-group-item" ]
                [ text ("Type: " ++ server.serverMetadata.name) ]
              , li [ class "list-group-item" ]
                [ text ("Status: " ++ server.serverMetadata.status) ]
              ]
            ]
        , div [ ]
            [ h2 [ ] [ text "CPU Usage" ]
            , img [ src "http://localhost:1192/server/cpu.png" ] []
            ]
        , div [ ]
            [ h2 [ ] [ text "Memory Usage" ]
            , img [ src "http://localhost:1192/server/memory.png" ] []
            ]
        ]


-- UPDATE

type Msg
    = CompletedServerLoad (Result Http.Error Server)

update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        CompletedServerLoad (Ok server) ->
            ( { model | server = Loaded server }, Cmd.none )
        CompletedServerLoad (Err error) ->
            ( { model | server = Failed }
            , Log.error
            )

-- HTTP

fetchServer : Task Http.Error Server
fetchServer =
    let
        decoder =
            Server.decoder
        query = [ ]
    in
        Http.task
            { method = "GET"
            , headers = Api.headers
            , url = Api.url ["server"] query
            , body = Http.emptyBody
            , resolver = Api.jsonResolver decoder
            , timeout = Nothing
            }

-- SUBSCRIPTION

subscriptions : Model -> Sub Msg
subscriptions model =
    Sub.none
