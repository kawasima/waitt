module Page.Env exposing (Model, Msg, init, update, view)

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

import Model.Env as Env exposing (Env, KeyValuePair)

-- MODEL

type alias Model =
    { env : Status Env
    }

type Status a
    = Loading
    | Loaded a
    | Failed

init : ( Model, Cmd Msg )
init =
    ( { env = Loading
      }
    , Task.attempt CompletedEnvLoad fetchEnv
    )

-- VIEW

view : Model -> { title : String, content : Html Msg }
view model =
    { title = "Env dump / WAITT dashboard"
    , content =
        case model.env of
            Loading -> div [ ] [ text "loading..." ]
            Loaded env -> viewEnv env
            Failed  -> div [ ] [ text "loading failed" ]
    }

viewEnv : Env -> Html Msg
viewEnv env =
    div [ class "three-forths column" ]
        [ h2 [ ]
          [ i [ class "meta-octicon octico-dashboard" ] [ ]
          , text "Enviromnent Variables"
          ]
        , div [ ]
              [ table [ class "ui celled table" ]
                [ thead []
                  [ tr []
                    [ th [] [ text "Name" ]
                    , th [] [ text "Value" ]
                    ]
                  ]
                , tbody [ ]
                    (List.map viewKeyValuePair env.environments)
                ]
              ]
        , h2 [ ]
          [ i [ class "meta-octicon octico-dashboard" ] [ ]
          , text "System properties"
          ]
        , div [ ]
              [ table [ class "ui celled table" ]
                [ thead []
                  [ tr []
                    [ th [] [ text "Name" ]
                    , th [] [ text "Value" ]
                    ]
                  ]
                , tbody [ ]
                    (List.map viewKeyValuePair env.properties)
                ]
              ]        ]

viewKeyValuePair : KeyValuePair -> Html Msg
viewKeyValuePair kv =
    tr [ ]
        [ td [] [ text kv.key ]
        , td [] [ text kv.value ]
        ]

-- UPDATE

type Msg
    = CompletedEnvLoad (Result Http.Error Env)

update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        CompletedEnvLoad (Ok env) ->
            ( { model | env = Loaded env }, Cmd.none )
        CompletedEnvLoad (Err error) ->
            ( { model | env = Failed }
            , Log.error
            )

-- HTTP

fetchEnv : Task Http.Error Env
fetchEnv =
    let
        decoder =
            Env.decoder
        query = [ ]
    in
        Http.task
            { method = "GET"
            , headers = Api.headers
            , url = Api.url ["env"] query
            , body = Http.emptyBody
            , resolver = Api.jsonResolver decoder
            , timeout = Nothing
            }

-- SUBSCRIPTION

subscriptions : Model -> Sub Msg
subscriptions model =
    Sub.none
