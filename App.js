import { StatusBar } from 'expo-status-bar';
import React, { useState,useEffect } from 'react';
import { StyleSheet, Text, View, Button, Image, FlatList, TouchableOpacity, Pressable, NativeModules, TextInput, SafeAreaView,NativeEventEmitter  } from 'react-native';
import 'react-native-gesture-handler';
import { NavigationContainer } from '@react-navigation/native';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import Ionicons from 'react-native-vector-icons/Ionicons';

import Goal from './goal';
import HRActivityModule from './HRA_Module';


const Tab = createBottomTabNavigator();

const MenuItems = [
    {id: 1, title: 'Goal'},
    {id: 2, title: 'Statistic'},
    {id: 3, title: 'Connection'},
    ];


  const Item = ({item, title, onPress, backgroundColor}) => (
    <Pressable style={[styles.menu_item,{backgroundColor}]} onPress={onPress}>
      <Text style={styles.Title}>{item.title}</Text>
    </Pressable>
   );


const onPressGoal = (navigation) => {
        navigation.navigate('Goal');
    }



const Home = ({navigation}) => {

 const [menu_list, setShowMenu] = useState(false);
  const [SelectedId, setSelectedId] = useState();

  const toggleMenu = () => {
    setShowMenu(!menu_list);
  };

  const renderItem = ({item}) => {
    const backgroundColor = item.id === SelectedId ? '#ff0000' : '#ffffff';
    const onPress = item.title === 'Goal' ? () => onPressGoal(navigation) : undefined; //{() => setSelectedId(item.id)}}

    return (
        <Item
          item = {item}
         onPress={onPress} //{() => setSelectedId(item.id)}}
          backgroundColor = {backgroundColor}
          />
      );
  };
  const renderMenuItems = () => {
    if (menu_list) {
      return (
        <FlatList
          data={MenuItems}
          renderItem={renderItem}
          keyExtractor={(item) => item.id.toString()}
          extraData = {SelectedId}
        />
      );
    } else {
      return null;
    }
  };

  const Cat = () => {

    return (
    <View>
      <Button title="Back"/>
      <Button title="Setting"/>

      <Image source={{uri:'https://cdn-icons-png.flaticon.com/512/3479/3479853.png'}}
      style={{width:200, height:200}}/>

      <Text>hr</Text>
      <Text>Steps</Text>
      <Text>Position</Text>

      <Button title="Menu" onPress={toggleMenu} />

      {renderMenuItems()}
    </View>
    );
   };

    return <Cat/>
}

/*
    const checkBluetooth = () => {
        HRActivityModule.checkBT();
    };*/
const App = () => {

   const [isBluetoothEnabled, setIsBluetoothEnabled] = useState(false);
   const [IsActivated, setIsActivated] = useState(false);
   const [IsConnected,setIsConnected] =useState(false) ;
   const [HR_allowed,setHR_allowed] = useState(false);
   const [HR_Value,setHR_Value] =useState(0) ;
   const [TextId,setTextId] =useState("9E265923") ;
   const [DeviceIdInput, setDeviceIdInput] = useState(false);

   const checkBluetooth = () => {
       HRActivityModule.checkBT();
     };

     const initializeAndConnect = () => {
         if(IsConnected==false) {

            HRActivityModule.initializeHRActivity()
               .then((result) => {
                   console.log(result); // "HRActivityModule initialized successfully."
                   setIsConnected(true);
                   return HRActivityModule.connectDevice();
               })
               .then(() => {
                   console.log('Device connected.');
               })
               .catch((error) => {
                   console.error(error);
               });
         }

         else {
            setIsConnected(false);
            return HRActivityModule.disconnectFromDevice(HRActivityModule.GetPolarId);
         }
     };

     const EndEditing = () => {
       HRActivityModule.SetPolarID(TextId);
       setDeviceIdInput(false);
     };
    /*
     const StreamHeartRate = () => {
            HRActivityModule.streamHR();
          };
*/

  return (
 /* <NavigationContainer>
    <Tab.Navigator>
      <Tab.Screen name = "Home" component  = {Home}/>
      <Tab.Screen name = "Goal" component  = {Goal}/>
    </Tab.Navigator>
  </NavigationContainer>
  */
    <SafeAreaView>
        <Text>Bluetooth: {isBluetoothEnabled ? 'Enabled' : 'Disabled'}</Text>
        <Button title="Check Bluetooth" onPress={() => HRActivityModule.checkBT()} />
        <Button title="init and connect" onPress={() => initializeAndConnect()} />
        <Button title="HR_Value" onPress={() =>  setHR_Value(HRActivityModule.getHeartRateValue())} />
        <Button title="Connect device" onPress={() =>
        HRActivityModule.connectDevice(TextId)
            .then((result) => {
                    console.log('Device connected:', result.deviceId);
                    // Do something with the connected device
                  })
                  .catch((error) => {
                    console.error('Error connecting device:', error.message);
                    // Handle the error
                  })
              }

        />
        <Button title="Disconnect device" onPress={() => HRActivityModule.disconnectDevice()} />
        <Button title="change deviceID" onPress={() => setDeviceIdInput(true)} />
        {DeviceIdInput && (
            <TextInput
                style = {styles.T_Input}
                placeholder = "Give the device ID"
                onChangeText = {text => setTextId(text)}
                onEndEditing = {() => EndEditing}
            />
        )}
        <Button title="StreamHr" onPress={() => HRActivityModule.streamHR()} />

        <Text>
            HR is: {HR_Value}
         </Text>
     </SafeAreaView>

  );
};

const styles = StyleSheet.create({
  menu_item:{
    padding : 10,
    marginVertical:10,
    marginHorizontal:10,
  },

  Title:{
    fontSize: 20
  },

  T_Input: {
    height: 40,
    margin: 12,
    borderWidth: 1,
    padding: 10,
  }
});

export default App;

