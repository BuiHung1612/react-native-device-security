Pod::Spec.new do |s|
  s.name           = 'react-native-device-security'
  s.version        = '1.0.0'
  s.summary        = 'Multi-layer device security detection for React Native'
  s.description    = <<-DESC
    React Native library for detecting device security threats including root detection,
    hook detection (Frida, Xposed, Magisk), debugger detection, and emulator detection.
    DESC
  s.homepage       = 'https://github.com/your-org/react-native-device-security'
  s.license        = { :type => 'MIT', :file => 'LICENSE' }
  s.author         = { 'OSP' => 'dev@osp.vn' }
  s.platforms      = { :ios => '12.0' }
  s.source         = { :git => 'https://github.com/your-org/react-native-device-security.git', :tag => "v#{s.version}" }
  s.source_files   = 'ios/**/*.{h,m,mm,swift}'
  s.requires_arc   = true

  s.dependency 'React-Core'
end
